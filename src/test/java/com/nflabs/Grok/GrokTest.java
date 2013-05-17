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
		String a =  g.discover("64.242.88.10 - - [07/Mar/2004:16:05:49 -0800] \"GET /twiki/bin/edit/Main/Double_bounce_sender?topicparent=Main.ConfigurationVariables HTTP/1.1\" 401 12846 " );	
		g.compile(a);
		Match gm = g.match("64.242.88.10 - - [07/Mar/2004:16:05:49 -0800] \"GET /twiki/bin/edit/Main/Double_bounce_sender?topicparent=Main.ConfigurationVariables HTTP/1.1\" 401 12846 ");
		gm.captures();
		System.out.println(gm.toJson());


		//lol
		/*g.compile("%{URI}");
			
		Match gm = g.match("http://www.google.com/search=lol");
		gm.captures();
		
		Map<String, String> map = gm.toMap();
		System.out.println("lol");*/
		/*assertEquals("www.google.com", map.get("HOSTNAME"));
		assertEquals("www.google.com", map.get("host"));
		assertEquals("http", map.get("proto"));
		assertEquals(null, map.get("port"));
		assertEquals("/search=lol", map.get("params"));*/
	}
}