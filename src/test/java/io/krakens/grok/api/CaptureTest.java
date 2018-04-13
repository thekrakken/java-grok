package io.krakens.grok.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import io.krakens.grok.api.exception.GrokException;

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
    Match match = grok.match("Hello World");
    assertEquals("(?<name0>.*)", grok.getNamedRegex());
    assertEquals("Hello World", match.getSubject());
    match.captures();
    assertEquals(1, match.toMap().size());
    assertEquals("Hello World", match.toMap().get("foo"));
    assertEquals("{foo=Hello World}", match.toMap().toString());
  }

  @Test
  public void test002_captureMethodMulti() throws GrokException {
    grok.addPattern("foo", ".*");
    grok.addPattern("bar", ".*");
    grok.compile("%{foo} %{bar}");
    Match match = grok.match("Hello World");
    assertEquals("(?<name0>.*) (?<name1>.*)", grok.getNamedRegex());
    assertEquals("Hello World", match.getSubject());
    match.captures();
    assertEquals(2, match.toMap().size());
    assertEquals("Hello", match.toMap().get("foo"));
    assertEquals("World", match.toMap().get("bar"));
    assertEquals("{bar=World, foo=Hello}", match.toMap().toString());
  }

  @Test
  public void test003_captureMathodNasted() throws GrokException {
    grok.addPattern("foo", "\\w+ %{bar}");
    grok.addPattern("bar", "\\w+");
    grok.compile("%{foo}");
    Match match = grok.match("Hello World");
    assertEquals("(?<name0>\\w+ (?<name1>\\w+))", grok.getNamedRegex());
    assertEquals("Hello World", match.getSubject());
    match.captures();
    assertEquals(2, match.toMap().size());
    assertEquals("Hello World", match.toMap().get("foo"));
    assertEquals("World", match.toMap().get("bar"));
    assertEquals("{bar=World, foo=Hello World}", match.toMap().toString());
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
    Match match = grok.match("Hello");
    match.captures();
    assertEquals(1, match.toMap().size());
    assertEquals("Hello", match.toMap().get(subname).toString());
    assertEquals("{abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_abcdef=Hello}",
        match.toMap().toString());
  }

  @Test
  public void test006_captureOnlyNamed() throws GrokException {
    grok.addPattern("abcdef", "[a-zA-Z]+");
    grok.addPattern("ghijk", "\\d+");
    grok.compile("%{abcdef:abcdef}%{ghijk}", true);
    Match match = grok.match("abcdef12345");
    match.captures();
    assertEquals(match.toMap().size(), 1);
    assertNull(match.toMap().get("ghijk"));
    assertEquals(match.toMap().get("abcdef"), "abcdef");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test007_captureDuplicateName() throws GrokException {
    grok.compile("%{INT:id} %{INT:id}");
    Match match = grok.match("123 456");
    match.captures();
    assertEquals(match.toMap().size(), 1);
    assertEquals(((List<Object>) (match.toMap().get("id"))).size(), 2);
    assertEquals(((List<Object>) (match.toMap().get("id"))).get(0), "123");
    assertEquals(((List<Object>) (match.toMap().get("id"))).get(1), "456");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test008_flattenDuplicateKeys() throws GrokException {
    grok.compile("(?:foo %{INT:id} bar|bar %{INT:id} foo)");
    Match match = grok.match("foo 123 bar");
    match.capturesFlattened();
    assertEquals(match.toMap().size(), 1);
    assertEquals(match.toMap().get("id"), "123");
    Match match2 = grok.match("bar 123 foo");
    match2.capturesFlattened();
    assertEquals(match2.toMap().size(), 1);
    assertEquals(match2.toMap().get("id"), "123");

    grok.compile("%{INT:id} %{INT:id}");
    Match match1 = grok.match("123 456");

    try {
      match1.capturesFlattened();
      fail("should report error due tu ambiguity");
    } catch (RuntimeException e) {
      assertThat(e.getMessage(), containsString("has multiple non-null values, this is not allowed in flattened mode"));
    }
  }

}
