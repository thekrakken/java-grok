package io.thekraken.grok.api;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static org.junit.Assert.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GrokTest {


    /**
     * Do some basic test
     *
     * @throws Throwable
     */
  /*
   * public void testGrok() throws Throwable { Grok g = new Grok();
   *
   * g.addPatternFromFile("patterns/base"); g.compile("%{APACHE}"); Match gm =
   * g.match("127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326"
   * ); //Match gm = g.match("10.192.1.47"); gm.captures(); //See the result
   * System.out.println(gm.toJson());
   *
   * }
   */
    private Grok g = Grok.EMPTY;

    @Test
    public void test000_basic() {
        Grok g = new Grok();
        boolean thrown = false;

        // expected exception
        try {
            g.addPatternFromFile("/good/luck");
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;

        try {
            g.addPattern(null, "");
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            g.copyPatterns(null);
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            g.copyPatterns(new HashMap<String, String>());
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void test000_dummy() throws Throwable {
        g.addPatternFromFile(ResourceManager.PATTERNS);
        boolean thrown = false;
        /** This check if grok throw */
        try {
            g.compile(null);
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);
        thrown = false;
        try {
            g.compile("");
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);
        thrown = false;
        try {
            g.compile("      ");
        } catch (GrokException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void test001_static_metod_factory() throws Throwable {

        Grok staticGrok = Grok.create(ResourceManager.PATTERNS, "%{USERNAME}");
        Match gm = staticGrok.match("root");
        gm.captures();
        assertEquals("{USERNAME=root}", gm.toMap().toString());

        gm = staticGrok.match("r00t");
        gm.captures();
        assertEquals("{USERNAME=r00t}", gm.toMap().toString());

        gm = staticGrok.match("guest");
        gm.captures();
        assertEquals("{USERNAME=guest}", gm.toMap().toString());

        gm = staticGrok.match("guest1234");
        gm.captures();
        assertEquals("{USERNAME=guest1234}", gm.toMap().toString());

        gm = staticGrok.match("john doe");
        gm.captures();
        assertEquals("{USERNAME=john}", gm.toMap().toString());
    }


    @Test
    public void test001_username() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
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

    @Test
    public void test001_username2() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
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

    @Test
    public void test002_numbers() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{NUMBER}");

        Match gm = g.match("-42");
        gm.captures();
        assertEquals("{NUMBER=-42}", gm.toMap().toString());

    }

    @Test
    public void test003_word() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{WORD}");

        Match gm = g.match("a");
        gm.captures();
        assertEquals("{WORD=a}", gm.toMap().toString());

        gm = g.match("abc");
        gm.captures();
        assertEquals("{WORD=abc}", gm.toMap().toString());

    }

    @Test
    public void test004_SPACE() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{SPACE}");

        Match gm = g.match("abc dc");
        gm.captures();
        assertEquals("{SPACE=}", gm.toMap().toString());

    }

    @Test
    public void test004_number() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{NUMBER}");

        Match gm = g.match("Something costs $55.4!");
        gm.captures();
        assertEquals("{NUMBER=55.4}", gm.toMap().toString());

    }

    @Test
    public void test005_NOTSPACE() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{NOTSPACE}");

        Match gm = g.match("abc dc");
        gm.captures();
        assertEquals("{NOTSPACE=abc}", gm.toMap().toString());

    }

    @Test
    public void test006_QUOTEDSTRING() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{QUOTEDSTRING:text}");

        Match gm = g.match("\"abc dc\"");
        gm.captures();
        assertEquals("{text=abc dc}", gm.toMap().toString());

    }

    @Test
    public void test007_UUID() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
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

    @Test
    public void test008_MAC() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{MAC}");

        Match gm = g.match("5E:FF:56:A2:AF:15");
        gm.captures();
        assertEquals("{MAC=5E:FF:56:A2:AF:15}", gm.toMap().toString());

    }

    @Test
    public void test009_IPORHOST() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{IPORHOST}");

        Match gm = g.match("www.google.fr");
        gm.captures();
        assertEquals("{IPORHOST=www.google.fr}", gm.toMap().toString());

        gm = g.match("www.google.com");
        gm.captures();
        assertEquals("{IPORHOST=www.google.com}", gm.toMap().toString());
    }

    @Test
    public void test010_HOSTPORT() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{HOSTPORT}");

        Match gm = g.match("www.google.fr:80");
        gm.captures();
        assertEquals("{HOSTPORT=www.google.fr:80, IPORHOST=www.google.fr, PORT=80}", gm.toMap()
                .toString());
    }

    @Test
    public void test011_COMBINEDAPACHELOG() throws Throwable {

        g.addPatternFromFile(ResourceManager.PATTERNS);
        g.compile("%{COMBINEDAPACHELOG}");

        Match gm =
                g.match("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"");
        gm.captures();
        assertNotNull(gm.toJson());
        assertEquals(
                gm.toMap().get("agent").toString(),
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22");
        assertEquals(gm.toMap().get("clientip").toString(), "112.169.19.192");
        assertEquals(gm.toMap().get("httpversion").toString(), "1.1");
        assertEquals(gm.toMap().get("timestamp").toString(), "06/Mar/2013:01:36:30 +0900");
        assertEquals(gm.toMap().get("TIME").toString(), "01:36:30");

        gm =
                g.match("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET /wp-content/plugins/easy-table/themes/default/style.css?ver=1.0 HTTP/1.1\" 304 - \"http://www.nflabs.com/\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"");
        gm.captures();
        assertNotNull(gm.toJson());
        // System.out.println(gm.toJson());
        assertEquals(
                gm.toMap().get("agent").toString(),
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22");
        assertEquals(gm.toMap().get("clientip").toString(), "112.169.19.192");
        assertEquals(gm.toMap().get("httpversion").toString(), "1.1");
        assertEquals(gm.toMap().get("request").toString(),
                "/wp-content/plugins/easy-table/themes/default/style.css?ver=1.0");
        assertEquals(gm.toMap().get("TIME").toString(), "01:36:30");

        // assertEquals("{HOSTPORT=www.google.fr:80, IPORHOST=www.google.fr, PORT=80}",
        // gm.toMap().toString());

    }

    /**
     * FROM HERE WE WILL USE STATIC GROK
     */

    @Test
    public void test012_day() throws Throwable {

        Grok grok = Grok.create(ResourceManager.PATTERNS, "%{DAY}");

        List<String> days = new ArrayList<String>();
        days.add("Mon");
        days.add("Monday");
        days.add("Tue");
        days.add("Tuesday");
        days.add("Wed");
        days.add("Wednesday");
        days.add("Thu");
        days.add("Thursday");
        days.add("Fri");
        days.add("Friday");
        days.add("Sat");
        days.add("Saturday");
        days.add("Sun");
        days.add("Sunday");

        int i = 0;
        for (String day : days) {
            Match m = grok.match(day);
            m.captures();
            assertNotNull(m.toMap());
            assertEquals(m.toMap().get("DAY"), days.get(i));
            i++;
        }
    }

    @Test
    public void test013_IpSet() throws Throwable {
        Grok grok = Grok.create(ResourceManager.PATTERNS, "%{IP}");

        BufferedReader br = new BufferedReader(new FileReader(ResourceManager.IP));
        String line;
        System.out.println("Starting test with ip");
        while ((line = br.readLine()) != null) {
            Match gm = grok.match(line);
            gm.captures();
            assertNotNull(gm.toJson());
            assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
            assertEquals(gm.toMap().get("IP"), line);
        }
    }

    @Test
    public void test014_month() throws Throwable {

        Grok grok = Grok.create(ResourceManager.PATTERNS, "%{MONTH}");

        String[] array = {"Jan", "January", "Feb", "February", "Mar", "March", "Apr", "April", "May", "Jun", "June",
                "Jul", "July", "Aug", "August", "Sep", "September", "Oct", "October", "Nov",
                "November", "Dec", "December"};
        List<String> months = new ArrayList<String>(Arrays.asList(array));
        int i = 0;
        for (String month : months) {
            Match m = grok.match(month);
            m.captures();
            assertNotNull(m.toMap());
            assertEquals(m.toMap().get("MONTH"), months.get(i));
            i++;
        }
    }

    @Test
    public void test015_iso8601() throws GrokException {
        Grok grok = Grok.create(ResourceManager.PATTERNS, "%{TIMESTAMP_ISO8601}");

        String[] array =
                {"2001-01-01T00:00:00",
                        "1974-03-02T04:09:09",
                        "2010-05-03T08:18:18+00:00",
                        "2004-07-04T12:27:27-00:00",
                        "2001-09-05T16:36:36+0000",
                        "2001-11-06T20:45:45-0000",
                        "2001-12-07T23:54:54Z",
                        "2001-01-01T00:00:00.123456",
                        "1974-03-02T04:09:09.123456",
                        "2010-05-03T08:18:18.123456+00:00",
                        "2004-07-04T12:27:27.123456-00:00",
                        "2001-09-05T16:36:36.123456+0000",
                        "2001-11-06T20:45:45.123456-0000",
                        "2001-12-07T23:54:54.123456Z"};

        List<String> times = new ArrayList<String>(Arrays.asList(array));
        int i = 0;
        for (String time : times) {
            Match m = grok.match(time);
            m.captures();
            assertNotNull(m.toMap());
            assertEquals(m.toMap().get("TIMESTAMP_ISO8601"), times.get(i));
            i++;
        }
    }

    @Test
    public void test016_uri() throws GrokException {
        Grok grok = Grok.create(ResourceManager.PATTERNS, "%{URI}");

        String[] array =
                {
                        "http://www.google.com",
                        "telnet://helloworld",
                        "http://www.example.com/",
                        "http://www.example.com/test.html",
                        "http://www.example.com/test.html?foo=bar",
                        "http://www.example.com/test.html?foo=bar&fizzle=baz",
                        "http://www.example.com:80/test.html?foo=bar&fizzle=baz",
                        "https://www.example.com:443/test.html?foo=bar&fizzle=baz",
                        "https://user@www.example.com:443/test.html?foo=bar&fizzle=baz",
                        "https://user:pass@somehost/fetch.pl",
                        "puppet:///",
                        "http://www.foo.com",
                        "http://www.foo.com/",
                        "http://www.foo.com/?testing",
                        "http://www.foo.com/?one=two",
                        "http://www.foo.com/?one=two&foo=bar",
                        "foo://somehost.com:12345",
                        "foo://user@somehost.com:12345",
                        "foo://user@somehost.com:12345/",
                        "foo://user@somehost.com:12345/foo.bar/baz/fizz",
                        "foo://user@somehost.com:12345/foo.bar/baz/fizz?test",
                        "foo://user@somehost.com:12345/foo.bar/baz/fizz?test=1&sink&foo=4",
                        "http://www.google.com/search?hl=en&source=hp&q=hello+world+%5E%40%23%24&btnG=Google+Search",
                        "http://www.freebsd.org/cgi/url.cgi?ports/sysutils/grok/pkg-descr",
                        "http://www.google.com/search?q=CAPTCHA+ssh&start=0&ie=utf-8&oe=utf-8&client=firefox-a&rls=org.mozilla:en-US:official",
                        "svn+ssh://somehost:12345/testing"};

        List<String> uris = new ArrayList<String>(Arrays.asList(array));
        int i = 0;
        for (String uri : uris) {
            Match m = grok.match(uri);
            m.captures();
            assertNotNull(m.toMap());
            assertEquals(m.toMap().get("URI"), uris.get(i));
            assertNotNull(m.toMap().get("URIPROTO"));
            i++;
        }
    }

    @Test
    public void test017_nonMachingList() throws GrokException {
        Grok grok = Grok.create(ResourceManager.PATTERNS, "%{URI}");

        String[] array =
                {
                        "http://www.google.com",
                        "telnet://helloworld",
                        "",
                        "svn+ssh://somehost:12345/testing"
                };
        List<String> uris = new ArrayList<String>(Arrays.asList(array));
        int i = 0;
        for (String uri : uris) {
            Match m = grok.match(uri);
            m.captures();
            assertNotNull(m.toMap());
            if (i == 2) {
                assertEquals(Collections.EMPTY_MAP, m.toMap());
            }
            i++;
        }
        assertEquals(i, 4);
    }

}
