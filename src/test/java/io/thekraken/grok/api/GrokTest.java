package io.thekraken.grok.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import io.thekraken.grok.api.exception.GrokException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GrokTest {
    GrokCompiler compiler;

    @Before
    public void setUp() throws Exception {
        compiler = GrokCompiler.newInstance();
        compiler.register(Resources.getResource(ResourceManager.PATTERNS).openStream());
    }

    @Test
    public void test000_basic() {
        GrokCompiler compiler = GrokCompiler.newInstance();
        boolean thrown = false;

        try {
            compiler.register(null, "");
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void test000_dummy() throws Throwable {
        boolean thrown = false;
        /** This check if grok throw */
        try {
            compiler.compile(null);
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
        thrown = false;
        try {
            compiler.compile("");
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
        thrown = false;
        try {
            compiler.compile("      ");
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void test001_static_metod_factory() throws Throwable {

        Grok staticGrok = compiler.compile("%{USERNAME}");
        Match gm = staticGrok.match("root");
        Map<String, Object> map = gm.capture();
        assertEquals("{USERNAME=root}", map.toString());

        gm = staticGrok.match("r00t");
        map = gm.capture();
        assertEquals("{USERNAME=r00t}", map.toString());

        gm = staticGrok.match("guest");
        map = gm.capture();
        assertEquals("{USERNAME=guest}", map.toString());

        gm = staticGrok.match("guest1234");
        map = gm.capture();
        assertEquals("{USERNAME=guest1234}", map.toString());

        gm = staticGrok.match("john doe");
        map = gm.capture();
        assertEquals("{USERNAME=john}", map.toString());
    }


    @Test
    public void test001_username() throws Throwable {
        Grok g = compiler.compile("%{USERNAME}");

        Match gm = g.match("root");
        Map<String, Object> map = gm.capture();
        assertEquals("{USERNAME=root}", map.toString());

        gm = g.match("r00t");
        map = gm.capture();
        assertEquals("{USERNAME=r00t}", map.toString());

        gm = g.match("guest");
        map = gm.capture();
        assertEquals("{USERNAME=guest}", map.toString());

        gm = g.match("guest1234");
        map = gm.capture();
        assertEquals("{USERNAME=guest1234}", map.toString());

        gm = g.match("john doe");
        map = gm.capture();
        assertEquals("{USERNAME=john}", map.toString());
    }

    @Test
    public void test001_username2() throws Throwable {
        Grok g = compiler.compile("%{USER}");

        Match gm = g.match("root");
        Map<String, Object> map = gm.capture();
        assertEquals("{USER=root}", map.toString());

        gm = g.match("r00t");
        map = gm.capture();
        assertEquals("{USER=r00t}", map.toString());

        gm = g.match("guest");
        map = gm.capture();
        assertEquals("{USER=guest}", map.toString());

        gm = g.match("guest1234");
        map = gm.capture();
        assertEquals("{USER=guest1234}", map.toString());

        gm = g.match("john doe");
        map = gm.capture();
        assertEquals("{USER=john}", map.toString());
    }

    @Test
    public void test002_numbers() throws Throwable {
        Grok g = compiler.compile("%{NUMBER}");

        Match gm = g.match("-42");
        Map<String, Object> map = gm.capture();
        assertEquals("{NUMBER=-42}", map.toString());

    }

    @Test
    public void test003_word() throws Throwable {
        Grok g = compiler.compile("%{WORD}");

        Match gm = g.match("a");
        Map<String, Object> map = gm.capture();
        assertEquals("{WORD=a}", map.toString());

        gm = g.match("abc");
        map = gm.capture();
        assertEquals("{WORD=abc}", map.toString());

    }

    @Test
    public void test004_SPACE() throws Throwable {
        Grok g = compiler.compile("%{SPACE}");

        Match gm = g.match("abc dc");
        Map<String, Object> map = gm.capture();
        assertEquals("{SPACE=}", map.toString());

    }

    @Test
    public void test004_number() throws Throwable {
        Grok g = compiler.compile("%{NUMBER}");

        Match gm = g.match("Something costs $55.4!");
        Map<String, Object> map = gm.capture();
        assertEquals("{NUMBER=55.4}", map.toString());

    }

    @Test
    public void test005_NOTSPACE() throws Throwable {
        Grok g = compiler.compile("%{NOTSPACE}");

        Match gm = g.match("abc dc");
        Map<String, Object> map = gm.capture();
        assertEquals("{NOTSPACE=abc}", map.toString());

    }

    @Test
    public void test006_QUOTEDSTRING() throws Throwable {
        Grok g = compiler.compile("%{QUOTEDSTRING:text}");

        Match gm = g.match("\"abc dc\"");
        Map<String, Object> map = gm.capture();
        assertEquals("{text=abc dc}", map.toString());
    }

    @Test
    public void test007_UUID() throws Throwable {
        Grok g = compiler.compile("%{UUID}");

        Match gm = g.match("61243740-4786-11e3-86a7-0002a5d5c51b");
        Map<String, Object> map = gm.capture();
        assertEquals("{UUID=61243740-4786-11e3-86a7-0002a5d5c51b}", map.toString());

        gm = g.match("7F8C7CB0-4786-11E3-8F96-0800200C9A66");
        map = gm.capture();
        assertEquals("{UUID=7F8C7CB0-4786-11E3-8F96-0800200C9A66}", map.toString());

        gm = g.match("03A8413C-F604-4D21-8F4D-24B19D98B5A7");
        map = gm.capture();
        assertEquals("{UUID=03A8413C-F604-4D21-8F4D-24B19D98B5A7}", map.toString());

    }

    @Test
    public void test008_MAC() throws Throwable {
        Grok g = compiler.compile("%{MAC}");

        Match gm = g.match("5E:FF:56:A2:AF:15");
        Map<String, Object> map = gm.capture();
        assertEquals("{MAC=5E:FF:56:A2:AF:15}", map.toString());

    }

    @Test
    public void test009_IPORHOST() throws Throwable {
        Grok g = compiler.compile("%{IPORHOST}");

        Match gm = g.match("www.google.fr");
        Map<String, Object> map = gm.capture();
        assertEquals("{IPORHOST=www.google.fr}", map.toString());

        gm = g.match("www.google.com");
        map = gm.capture();
        assertEquals("{IPORHOST=www.google.com}", map.toString());
    }

    @Test
    public void test010_HOSTPORT() throws Throwable {
        Grok g = compiler.compile("%{HOSTPORT}");

        Match gm = g.match("www.google.fr:80");
        Map<String, Object> map = gm.capture();
        assertEquals(ImmutableMap.of(
            "HOSTPORT", "www.google.fr:80",
            "IPORHOST", "www.google.fr",
            "PORT", "80"), map);
    }

    @Test
    public void test011_COMBINEDAPACHELOG() throws Throwable {
        Grok g = compiler.compile("%{COMBINEDAPACHELOG}");

        Match gm =
                g.match("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"");
        Map<String, Object> map = gm.capture();
        assertNotNull(gm.toJson());
        assertEquals(
                map.get("agent").toString(),
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22");
        assertEquals(map.get("clientip").toString(), "112.169.19.192");
        assertEquals(map.get("httpversion").toString(), "1.1");
        assertEquals(map.get("timestamp").toString(), "06/Mar/2013:01:36:30 +0900");
        assertEquals(map.get("TIME").toString(), "01:36:30");

        gm =
                g.match("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET /wp-content/plugins/easy-table/themes/default/style.css?ver=1.0 HTTP/1.1\" 304 - \"http://www.nflabs.com/\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"");
        map = gm.capture();
        assertNotNull(gm.toJson());
        // System.out.println(gm.toJson());
        assertEquals(
                map.get("agent").toString(),
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22");
        assertEquals(map.get("clientip").toString(), "112.169.19.192");
        assertEquals(map.get("httpversion").toString(), "1.1");
        assertEquals(map.get("request").toString(),
                "/wp-content/plugins/easy-table/themes/default/style.css?ver=1.0");
        assertEquals(map.get("TIME").toString(), "01:36:30");

        // assertEquals("{HOSTPORT=www.google.fr:80, IPORHOST=www.google.fr, PORT=80}",
        // map.toString());

    }

    /**
     * FROM HERE WE WILL USE STATIC GROK
     */

    @Test
    public void test012_day() throws Throwable {

        Grok grok = compiler.compile("%{DAY}");

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
            Map<String, Object> map = m.capture();
            assertNotNull(map);
            assertEquals(map.get("DAY"), days.get(i));
            i++;
        }
    }

    @Test
    public void test013_IpSet() throws Throwable {
        Grok grok = compiler.compile("%{IP}");

        BufferedReader br = new BufferedReader(new FileReader(ResourceManager.IP));
        String line;
        System.out.println("Starting test with ip");
        while ((line = br.readLine()) != null) {
            Match gm = grok.match(line);
            Map<String, Object> map = gm.capture();
            assertNotNull(gm.toJson());
            assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
            assertEquals(map.get("IP"), line);
        }
    }

    @Test
    public void test014_month() throws Throwable {

        Grok grok = compiler.compile("%{MONTH}");

        String[] array = {"Jan", "January", "Feb", "February", "Mar", "March", "Apr", "April", "May", "Jun", "June",
                "Jul", "July", "Aug", "August", "Sep", "September", "Oct", "October", "Nov",
                "November", "Dec", "December"};
        List<String> months = new ArrayList<String>(Arrays.asList(array));
        int i = 0;
        for (String month : months) {
            Match m = grok.match(month);
            Map<String, Object> map = m.capture();
            assertNotNull(map);
            assertEquals(map.get("MONTH"), months.get(i));
            i++;
        }
    }

    @Test
    public void test015_iso8601() throws GrokException {
        Grok grok = compiler.compile("%{TIMESTAMP_ISO8601}");

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
            Map<String, Object> map = m.capture();
            assertNotNull(map);
            assertEquals(map.get("TIMESTAMP_ISO8601"), times.get(i));
            i++;
        }
    }

    @Test
    public void test016_uri() throws GrokException {
        Grok grok = compiler.compile("%{URI}");

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
            Map<String, Object> map = m.capture();
            assertNotNull(map);
            assertEquals(map.get("URI"), uris.get(i));
            assertNotNull(map.get("URIPROTO"));
            i++;
        }
    }

    @Test
    public void test017_nonMachingList() throws GrokException {
        Grok grok = compiler.compile("%{URI}");

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
            Map<String, Object> map = m.capture();
            assertNotNull(map);
            if (i == 2) {
                assertEquals(Collections.EMPTY_MAP, map);
            }
            i++;
        }
        assertEquals(i, 4);
    }

    @Test
    public void test018_namedOnlySimpleCase() throws GrokException {
        compiler.register("WORD", "foo|bar");
        compiler.register("TEXT", "<< %{WORD}+ >>");

        Grok g = compiler.compile("%{TEXT:text}", true);

        String text = "<< barfoobarfoo >>";
        Match match = g.match(text);
        Map<String, Object> map = match.capture();
        assertEquals("unable to parse: " + text,
                text,
                map.get("text"));
    }

    @Test
    public void test019_namedOnlyAllCases() throws GrokException {
        /* like previous test, but systematic all four possible options */
        testPatternRepetitions(true, "(?:foo|bar)");
        testPatternRepetitions(true, "foo|bar");
        testPatternRepetitions(false, "(?:foo|bar)");
        testPatternRepetitions(false, "foo|bar");
    }

    private void testPatternRepetitions(boolean namedOnly, String pattern) throws GrokException {
        String description = format("[readonly:%s pattern:%s] ", namedOnly, pattern);;

        compiler.register("WORD", pattern);
        compiler.register("TEXT", "<< %{WORD}+ >>");

        Grok grok = compiler.compile("%{TEXT:text}", namedOnly);
        assertMatches(description, grok, "<< foo >>");
        assertMatches(description, grok, "<< foobar >>");
        assertMatches(description, grok, "<< foofoobarbar >>");
        assertMatches(description, grok, "<< barfoobarfoo >>");
    }

    private void assertMatches(String description, Grok grok, String text) {
        Match match = grok.match(text);
        Map<String, Object> map = match.capture();
        assertEquals(format("%s: unable to parse '%s'", description, text),
                text,
                map.get("text"));
    }

    @Test
    public void test020_postfix_patterns() throws Throwable {
        GrokCompiler compiler = GrokCompiler.newInstance();
        compiler.register(Resources.getResource("patterns/postfix").openStream());
        compiler.register(Resources.getResource("patterns/patterns").openStream());
        Grok grok = compiler.compile("%{POSTFIX_SMTPD}", false);

        assertTrue(grok.getPatterns().containsKey("POSTFIX_SMTPD"));
    }

    @Test
    public void test021_postfix_patterns_with_named_captures_only() throws Throwable {
        GrokCompiler compiler = GrokCompiler.newInstance();
        compiler.register(Resources.getResource("patterns/postfix").openStream());
        compiler.register(Resources.getResource("patterns/patterns").openStream());
        Grok grok = compiler.compile("%{POSTFIX_SMTPD}", true);

        assertTrue(grok.getPatterns().containsKey("POSTFIX_SMTPD"));
    }

    @Test
    public void test022_named_captures_with_missing_definition() throws Throwable {
        ensureAbortsWithDefinitionMissing("FOO %{BAR}", "%{FOO}", true);
    }

    @Test
    public void test023_captures_with_missing_definition() throws Throwable {
        ensureAbortsWithDefinitionMissing("FOO %{BAR}", "%{FOO:name}", false);
    }

    @Test
    public void test024_captures_with_missing_definition() throws Throwable {
        ensureAbortsWithDefinitionMissing("FOO %{BAR}", "%{FOO}", false);
    }

    private void ensureAbortsWithDefinitionMissing(String pattern, String compilePattern, boolean namedOnly) {
        try {
            compiler.compile(pattern);
            compiler.compile(compilePattern, namedOnly);
            fail("should abort due to missing definition");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("No definition for key"));
        }
    }

    @Test
    public void testGroupTypes() throws Exception {
        Grok grok = compiler.compile("%{HTTPDATE:timestamp;date;dd/MMM/yyyy:HH:mm:ss Z} %{USERNAME:username:meta} %{IPORHOST:host}:%{POSINT:port:integer}", true);
        assertEquals(Converter.Type.DATETIME, grok.groupTypes.get("timestamp"));
        assertEquals(Converter.Type.META, grok.groupTypes.get("username"));
        assertEquals(Converter.Type.INT, grok.groupTypes.get("port"));
        assertNull(grok.groupTypes.get("host"));

       Match match = grok.match("07/Mar/2004:16:45:56 -0800 test 64.242.88.10:8080");
       Map<String, Object> result = match.capture();
       assertEquals("test", result.get("username"));
       assertEquals("64.242.88.10", result.get("host"));
       assertEquals(8080, result.get("port"));
       assertTrue(result.get("timestamp") instanceof Instant);
    }

    @Test
    public void testTimeZone() throws Exception {
        // no timezone. default to UTC
        String date = "03/19/2018 14:11:00";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        Grok grok = compiler.compile("%{DATESTAMP:timestamp;date;MM/dd/yyyy HH:mm:ss}", true);
        Instant instant = (Instant) grok.match(date).capture().get("timestamp");
        assertEquals(ZonedDateTime.parse(date, dtf.withZone(ZoneOffset.UTC)).toInstant(), instant);

        // set default timezone to PST
        ZoneId PST = ZoneId.of("PST", ZoneId.SHORT_IDS);
        grok = compiler.compile("%{DATESTAMP:timestamp;date;MM/dd/yyyy HH:mm:ss}", PST, true);
        instant = (Instant) grok.match(date).capture().get("timestamp");
        assertEquals(ZonedDateTime.parse(date, dtf.withZone(PST)).toInstant(), instant);

        // when timestamp has timezone, use it instead of the default.
        String dateWithTimeZone = "07/Mar/2004:16:45:56 +0800";
        dtf = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
        grok = compiler.compile("%{HTTPDATE:timestamp;date;dd/MMM/yyyy:HH:mm:ss Z}", PST, true);
        instant = (Instant) grok.match(dateWithTimeZone).capture().get("timestamp");
        assertEquals(ZonedDateTime.parse(dateWithTimeZone, dtf.withZone(ZoneOffset.ofHours(8))).toInstant(), instant);
    }
}
