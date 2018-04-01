package io.thekraken.grok.api;

import com.google.common.io.Resources;
import io.thekraken.grok.api.exception.GrokException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApacheDataTypeTest {
    private final String line = "64.242.88.10 - - [07/Mar/2004:16:45:56 -0800] \"GET /twiki/bin/attach/Main/PostfixCommands HTTP/1.1\" 401 12846";

    private GrokCompiler compiler;

    @Before
    public void setup() throws Exception {
        compiler = GrokCompiler.newInstance();
        compiler.register(Resources.getResource(ResourceManager.PATTERNS).openStream());
    }

    @Test
    public void test002_httpd_access_semi() throws GrokException, IOException, ParseException {
        Grok g = compiler.compile( "%{IPORHOST:clientip} %{USER:ident;boolean} %{USER:auth} \\[%{HTTPDATE:timestamp;date;dd/MMM/yyyy:HH:mm:ss Z}\\] \"(?:%{WORD:verb;string} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion;float})?|%{DATA:rawrequest})\" %{NUMBER:response;int} (?:%{NUMBER:bytes;long}|-)");

        System.out.println(line);
        Match gm = g.match(line);
        Map<String, Object> map = gm.capture();

        assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
        Instant ts = ZonedDateTime.of(2004, 03, 07, 16, 45, 56, 0, ZoneOffset.ofHours(-8)).toInstant();
        assertTrue(map.get("timestamp").equals(ts));
        assertTrue(map.get("response").equals(Integer.valueOf(401)));
        assertTrue(map.get("ident").equals(Boolean.FALSE));
        assertTrue(map.get("httpversion").equals(Float.valueOf(1.1f)));
        assertTrue(map.get("bytes").equals(Long.valueOf(12846)));
        assertTrue(map.get("verb").equals("GET"));

    }

    @Test
    public void test002_httpd_access_colon() throws GrokException, IOException, ParseException {
        Grok g = compiler.compile("%{IPORHOST:clientip} %{USER:ident:boolean} %{USER:auth} \\[%{HTTPDATE:timestamp:date:dd/MMM/yyyy:HH:mm:ss Z}\\] \"(?:%{WORD:verb:string} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion:float})?|%{DATA:rawrequest})\" %{NUMBER:response:int} (?:%{NUMBER:bytes:long}|-)");

        Match gm = g.match(line);
        Map<String, Object> map = gm.capture();

        assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());

        Instant ts = ZonedDateTime.of(2004, 03, 07, 16, 45, 56, 0, ZoneOffset.ofHours(-8)).toInstant();
        assertTrue(map.get("timestamp").equals(ts));
        assertTrue(map.get("response").equals(Integer.valueOf(401)));
        assertTrue(map.get("ident").equals(Boolean.FALSE));
        assertTrue(map.get("httpversion").equals(Float.valueOf(1.1f)));
        assertTrue(map.get("bytes").equals(Long.valueOf(12846)));
        assertTrue(map.get("verb").equals("GET"));

    }

}
