package com.nflabs.Grok;

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
	/*public void testGrok() throws Throwable {
		Grok g = new Grok();

		g.addPatternFromFile("patterns/base");
		g.compile("%{APACHE}");
		Match gm = g.match("127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326");
		//Match gm = g.match("10.192.1.47");
		gm.captures();
		//See the result
		System.out.println(gm.toJson());

	}*/
	private Grok g = new Grok();

	public void test001_username() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{USERNAME}");

		Match gm = g.match("root");
		gm.captures();
		assertEquals("{USERNAME=root}", gm.toMap().toString());

		gm = g.match("r00t");
		gm.captures();
		assertEquals("{USERNAME=r00t}", gm.toMap().toString());

		gm = g.match("guest");
		gm.captures();
		assertEquals("{USERNAME=guest}", gm.toMap().toString());

		gm = g.match("guest1234");
		gm.captures();
		assertEquals("{USERNAME=guest1234}", gm.toMap().toString());

		gm = g.match("john doe");
		gm.captures();
		assertEquals("{USERNAME=john}", gm.toMap().toString());
	}

	public void test001_username2() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{USER}");

		Match gm = g.match("root");
		gm.captures();
		assertEquals("{USER=root}", gm.toMap().toString());

		gm = g.match("r00t");
		gm.captures();
		assertEquals("{USER=r00t}", gm.toMap().toString());

		gm = g.match("guest");
		gm.captures();
		assertEquals("{USER=guest}", gm.toMap().toString());

		gm = g.match("guest1234");
		gm.captures();
		assertEquals("{USER=guest1234}", gm.toMap().toString());

		gm = g.match("john doe");
		gm.captures();
		assertEquals("{USER=john}", gm.toMap().toString());
	}

	public void test002_numbers() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{NUMBER}");

		Match gm = g.match("-42");
		gm.captures();
		assertEquals("{NUMBER=-42}", gm.toMap().toString());

	}

	public void test003_word() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{WORD}");

		Match gm = g.match("a");
		gm.captures();
		assertEquals("{WORD=a}", gm.toMap().toString());
		
		gm = g.match("abc");
		gm.captures();
		assertEquals("{WORD=abc}", gm.toMap().toString());

	}
	
	public void test004_SPACE() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{SPACE}");

		Match gm = g.match("abc dc");
		gm.captures();
		assertEquals("{SPACE=}", gm.toMap().toString());

	}
	
	public void test005_NOTSPACE() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{NOTSPACE}");

		Match gm = g.match("abc dc");
		gm.captures();
		assertEquals("{NOTSPACE=abc}", gm.toMap().toString());

	}
	
	public void test006_QUOTEDSTRING() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{QUOTEDSTRING:text}");

		Match gm = g.match("\"abc dc\"");
		gm.captures();
		assertEquals("{text=abc dc}", gm.toMap().toString());

	}
	
	public void test007_UUID() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{UUID}");

		Match gm = g.match("61243740-4786-11e3-86a7-0002a5d5c51b");
		gm.captures();
		assertEquals("{UUID=61243740-4786-11e3-86a7-0002a5d5c51b}", gm.toMap().toString());
		
		gm = g.match("7F8C7CB0-4786-11E3-8F96-0800200C9A66");
		gm.captures();
		assertEquals("{UUID=7F8C7CB0-4786-11E3-8F96-0800200C9A66}", gm.toMap().toString());
		
		gm = g.match("03A8413C-F604-4D21-8F4D-24B19D98B5A7");
		gm.captures();
		assertEquals("{UUID=03A8413C-F604-4D21-8F4D-24B19D98B5A7}", gm.toMap().toString());

	}
	
	public void test008_MAC() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{MAC}");

		Match gm = g.match("5E:FF:56:A2:AF:15");
		gm.captures();
		assertEquals("{MAC=5E:FF:56:A2:AF:15}", gm.toMap().toString());

	}

	public void test009_IPORHOST() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{IPORHOST}");

		Match gm = g.match("www.google.fr");
		gm.captures();
		assertEquals("{IPORHOST=www.google.fr}", gm.toMap().toString());

	}
	
	public void test010_HOSTPORT() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{HOSTPORT}");

		Match gm = g.match("www.google.fr:80");
		gm.captures();
		assertEquals("{HOSTPORT=www.google.fr:80, IPORHOST=www.google.fr, PORT=80}", gm.toMap().toString());

	}
	public void test01COMBINEDAPACHELOG() throws Throwable{

		g.addPatternFromFile("patterns/patterns");
		g.compile("%{COMBINEDAPACHELOG}");

		Match gm = g.match("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"");
		gm.captures();
		assertNotNull(gm.toJson());
		System.out.println(gm.toJson());
		
		
		gm = g.match("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET /wp-content/plugins/easy-table/themes/default/style.css?ver=1.0 HTTP/1.1\" 304 - \"http://www.nflabs.com/\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"");
			gm.captures();
		assertNotNull(gm.toJson());
		System.out.println(gm.toJson());
		
		//assertEquals("{HOSTPORT=www.google.fr:80, IPORHOST=www.google.fr, PORT=80}", gm.toMap().toString());

	}

}