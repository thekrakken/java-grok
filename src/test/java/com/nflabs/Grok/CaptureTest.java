package com.nflabs.Grok;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nflabs.grok.Grok;
import com.nflabs.grok.GrokException;
import com.nflabs.grok.Match;

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
    assertEquals("(?<name0>.*)", grok.getExpandedPattern());
    assertEquals("Hello World", m.line);
    m.captures();
    assertEquals(1, m.toMap().size());
    assertEquals("{foo=Hello World}", m.toMap().toString());
  }

}
