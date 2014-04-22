package com.nflabs.Grok;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.nflabs.grok.Grok;
import com.nflabs.grok.GrokException;
import com.nflabs.grok.Match;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CaptureTest {

  static Grok grok;

  @BeforeClass
  public static void setUp() throws GrokException {

    grok = Grok.create("patterns/patterns", null);
  }

  @Test
  public void test001_captureMathod() throws GrokException {
    grok.addPattern("foo", ".*");
    grok.compile("%{foo}");
    Match m = grok.match("Hello World");
    assertEquals("(?<name0>.*)", grok.getNamedRegex());
    assertEquals("Hello World", m.line);
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
    assertEquals("Hello World", m.line);
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
    assertEquals("Hello World", m.line);
    m.captures();
    assertEquals(2, m.toMap().size());
    assertEquals("Hello World", m.toMap().get("foo"));
    assertEquals("World", m.toMap().get("bar"));
    assertEquals("{bar=World, foo=Hello World}", m.toMap().toString());
  }

  @Test
  public void test004_captureNastedRecustion() throws GrokException{
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
    String subname = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_abc:def";
    grok.addPattern(name, "\\w+");
    grok.compile("%{"+name+":"+subname+"}");
    Match m = grok.match("Hello");
    m.captures();
    assertEquals(1, m.toMap().size());
    assertEquals("Hello", m.toMap().get(subname).toString());
    assertEquals("{abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_abc:def=Hello}", m.toMap().toString());
  }

}
