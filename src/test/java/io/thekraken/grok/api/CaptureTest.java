package io.thekraken.grok.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import io.thekraken.grok.api.exception.GrokException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CaptureTest {

    static Grok grok;

    @BeforeClass
    public static void setUp() throws GrokException {

        grok = Grok.create(ResourceManager.PATTERNS, null);
    }

    @Test
    public void test001_captureMathod() throws GrokException {
        grok.addPattern("foo", ".*");
        grok.compile("%{foo}");
        Match m = grok.match("Hello World");
        assertEquals("(?<name0>.*)", grok.getNamedRegex());
        assertEquals("Hello World", m.getSubject());
        m.captures();
        assertEquals(1, m.toMap().size());
        assertEquals("Hello World", m.toMap().get("foo"));
        assertEquals("{foo=Hello World}", m.toMap().toString());
    }

    @Test
    public void test002_captureMathodMulti() throws GrokException {
        grok.addPattern("foo", ".*");
        grok.addPattern("bar", ".*");
        grok.compile("%{foo} %{bar}");
        Match m = grok.match("Hello World");
        assertEquals("(?<name0>.*) (?<name1>.*)", grok.getNamedRegex());
        assertEquals("Hello World", m.getSubject());
        m.captures();
        assertEquals(2, m.toMap().size());
        assertEquals("Hello", m.toMap().get("foo"));
        assertEquals("World", m.toMap().get("bar"));
        assertEquals("{bar=World, foo=Hello}", m.toMap().toString());
    }

    @Test
    public void test003_captureMathodNasted() throws GrokException {
        grok.addPattern("foo", "\\w+ %{bar}");
        grok.addPattern("bar", "\\w+");
        grok.compile("%{foo}");
        Match m = grok.match("Hello World");
        assertEquals("(?<name0>\\w+ (?<name1>\\w+))", grok.getNamedRegex());
        assertEquals("Hello World", m.getSubject());
        m.captures();
        assertEquals(2, m.toMap().size());
        assertEquals("Hello World", m.toMap().get("foo"));
        assertEquals("World", m.toMap().get("bar"));
        assertEquals("{bar=World, foo=Hello World}", m.toMap().toString());
    }

    @Test
    public void test004_captureNastedRecustion() throws GrokException {
        grok.addPattern("foo", "%{foo}");
        boolean thrown = false;
        /** Must raise `Deep recursion pattern` execption */
        try {
            grok.compile("%{foo}");
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void test005_captureSubName() throws GrokException {
        String name = "foo";
        String subname = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_abcdef";
        grok.addPattern(name, "\\w+");
        grok.compile("%{" + name + ":" + subname + "}");
        Match m = grok.match("Hello");
        m.captures();
        assertEquals(1, m.toMap().size());
        assertEquals("Hello", m.toMap().get(subname).toString());
        assertEquals("{abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_abcdef=Hello}", m.toMap().toString());
    }

    @Test
    public void test006_captureOnlyNamed() throws GrokException {
        grok.addPattern("abcdef", "[a-zA-Z]+");
        grok.addPattern("ghijk", "\\d+");
        grok.compile("%{abcdef:abcdef}%{ghijk}", true);
        Match m = grok.match("abcdef12345");
        m.captures();
        assertEquals(m.toMap().size(), 1);
        assertNull(m.toMap().get("ghijk"));
        assertEquals(m.toMap().get("abcdef"), "abcdef");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void test007_captureDuplicateName() throws GrokException {
    	grok.compile("%{INT:id} %{INT:id}");
    	Match m = grok.match("123 456");
    	m.captures();
    	assertEquals(m.toMap().size(), 1);
    	assertEquals(((List<Object>) (m.toMap().get("id"))).size(),2);
    	assertEquals(((List<Object>) (m.toMap().get("id"))).get(0),"123");
    	assertEquals(((List<Object>) (m.toMap().get("id"))).get(1),"456");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void test008_flattenDuplicateKeys() throws GrokException {
        grok.compile("(?:foo %{INT:id} bar|bar %{INT:id} foo)");
        Match m = grok.match("foo 123 bar");
        m.capturesFlattened();
        assertEquals(m.toMap().size(), 1);
        assertEquals(m.toMap().get("id"), "123");
        Match m2 = grok.match("bar 123 foo");
        m2.capturesFlattened();
        assertEquals(m2.toMap().size(), 1);
        assertEquals(m2.toMap().get("id"), "123");

        grok.compile("%{INT:id} %{INT:id}");
        Match m3 = grok.match("123 456");

        try {
            m3.capturesFlattened();
            fail("should report error due tu ambiguity");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("has multiple non-null values, this is not allowed in flattened mode"));
        }
    }

}
