package com.nflabs.Grok;

import java.util.Map;

import junit.framework.TestCase;

public class GrokTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Do some basic test
	 * 
	 * @throws Throwable
	 */
	public void testGrok() throws Throwable{
    	Grok g = new Grok();

		g.addPatternFromFile("patterns/base");
			
		g.compile("%{URI}");
			
		Match gm = g.match("http://www.google.com/search=lol");
		gm.captures();
		
		Map<String, String> map = gm.toMap();
		assertEquals("www.google.com", map.get("HOSTNAME"));
		assertEquals("www.google.com", map.get("host"));
		assertEquals("http", map.get("proto"));
		assertEquals(null, map.get("port"));
		assertEquals("/search=lol", map.get("params"));
	}
}
