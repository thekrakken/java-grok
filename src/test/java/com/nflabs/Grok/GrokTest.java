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
    public void testGrok() throws Throwable {
        Grok g = new Grok();

        g.addPatternFromFile("patterns/base");
        g.compile("%{APACHE}");
        Match gm = g.match("10.192.1.47 - - [23/May/2013:10:47:40] \"GET /flower1_store/category1.screen?category_id1=FLOWERS HTTP/1.1\" 200 10577 \"http://mystore.abc.com/flower1_store/main.screen&JSESSIONID=SD1SL10FF3ADFF3\" \"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.10) Gecko/20070223 CentOS/1.5.0.10-0.1.el4.centos Firefox/1.5.0.10\" 3823 404");
        if (gm != null) {
            gm.captures();
            //See the result
            System.out.println(gm.toJson());
        }
    }
}