package io.krakens.grok.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import io.krakens.grok.api.exception.GrokException;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CaptureTest {

  GrokCompiler compiler;

  @Before
  public void setUp() throws Exception {
    compiler = GrokCompiler.newInstance();
    compiler.register(Resources.getResource(ResourceManager.PATTERNS).openStream());
  }

  @Test
  public void test001_captureMathod() {
    compiler.register("foo", ".*");
    Grok grok = compiler.compile("%{foo}");
    Match match = grok.match("Hello World");
    assertEquals("(?<name0>.*)", grok.getNamedRegex());
    assertEquals("Hello World", match.getSubject());
    Map<String, Object> map = match.capture();
    assertEquals(1, map.size());
    assertEquals("Hello World", map.get("foo"));
    assertEquals("{foo=Hello World}", map.toString());
  }

  @Test
  public void test002_captureMathodMulti() throws GrokException {
    compiler.register("foo", ".*");
    compiler.register("bar", ".*");
    Grok grok = compiler.compile("%{foo} %{bar}");
    Match match = grok.match("Hello World");
    assertEquals("(?<name0>.*) (?<name1>.*)", grok.getNamedRegex());
    assertEquals("Hello World", match.getSubject());
    Map<String, Object> map = match.capture();
    assertEquals(2, map.size());
    assertEquals("Hello", map.get("foo"));
    assertEquals("World", map.get("bar"));
    assertEquals("{foo=Hello, bar=World}", map.toString());
  }

  @Test
  public void test003_captureMathodNasted() throws GrokException {
    compiler.register("foo", "\\w+ %{bar}");
    compiler.register("bar", "\\w+");
    Grok grok = compiler.compile("%{foo}");
    Match match = grok.match("Hello World");
    assertEquals("(?<name0>\\w+ (?<name1>\\w+))", grok.getNamedRegex());
    assertEquals("Hello World", match.getSubject());
    Map<String, Object> map = match.capture();
    assertEquals(2, map.size());
    assertEquals("Hello World", map.get("foo"));
    assertEquals("World", map.get("bar"));
    assertEquals("{foo=Hello World, bar=World}", map.toString());
  }

  @Test
  public void test004_captureNastedRecustion() throws GrokException {
    compiler.register("foo", "%{foo}");
    boolean thrown = false;
    /** Must raise `Deep recursion pattern` execption */
    try {
      compiler.compile("%{foo}");
    } catch (Exception e) {
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void test005_captureSubName() throws GrokException {
    String name = "foo";
    String subname = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_abcdef";
    compiler.register(name, "\\w+");
    Grok grok = compiler.compile("%{" + name + ":" + subname + "}");
    Match match = grok.match("Hello");
    Map<String, Object> map = match.capture();
    assertEquals(1, map.size());
    assertEquals("Hello", map.get(subname).toString());
    assertEquals("{abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_abcdef=Hello}",
        map.toString());
  }

  @Test
  public void test006_captureOnlyNamed() throws GrokException {
    compiler.register("abcdef", "[a-zA-Z]+");
    compiler.register("ghijk", "\\d+");
    Grok grok = compiler.compile("%{abcdef:abcdef}%{ghijk}", true);
    Match match = grok.match("abcdef12345");
    Map<String, Object> map = match.capture();
    assertEquals(map.size(), 1);
    assertNull(map.get("ghijk"));
    assertEquals(map.get("abcdef"), "abcdef");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test007_captureDuplicateName() throws GrokException {
    Grok grok = compiler.compile("%{INT:id} %{INT:id}");
    Match match = grok.match("123 456");
    Map<String, Object> map = match.capture();
    assertEquals(map.size(), 1);
    assertEquals(((List<Object>) (map.get("id"))).size(), 2);
    assertEquals(((List<Object>) (map.get("id"))).get(0), "123");
    assertEquals(((List<Object>) (map.get("id"))).get(1), "456");
  }

  @Test
  public void test008_flattenDuplicateKeys() throws GrokException {
    Grok grok = compiler.compile("(?:foo %{INT:id} bar|bar %{INT:id} foo)");
    Match match = grok.match("foo 123 bar");
    Map<String, Object> map = match.captureFlattened();
    assertEquals(map.size(), 1);
    assertEquals(map.get("id"), "123");
    Match m2 = grok.match("bar 123 foo");
    map = m2.captureFlattened();
    assertEquals(map.size(), 1);
    assertEquals(map.get("id"), "123");

    grok = compiler.compile("%{INT:id} %{INT:id}");
    Match m3 = grok.match("123 456");

    try {
      m3.captureFlattened();
      fail("should report error due tu ambiguity");
    } catch (RuntimeException e) {
      assertThat(e.getMessage(),
          containsString("has multiple non-null values, this is not allowed in flattened mode"));
    }
  }
}
