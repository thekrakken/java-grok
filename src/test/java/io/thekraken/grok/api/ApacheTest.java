package io.thekraken.grok.api;

import io.thekraken.grok.api.exception.GrokException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApacheTest {

    public final static String LOG_FILE = "src/test/resources/access_log";
    public final static String LOG_DIR_NASA = "src/test/resources/nasa/";

    @Test
    public void test001_httpd_access() throws GrokException, IOException {
        Grok g = Grok.create(ResourceManager.PATTERNS, "%{COMMONAPACHELOG}");

        BufferedReader br = new BufferedReader(new FileReader(LOG_FILE));
        String line;
        System.out.println("Starting test with httpd log");
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            Match gm = g.match(line);
            gm.captures();
            assertNotNull(gm.toJson());
            assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
        }
        br.close();
    }

    @Test
    public void test002_nasa_httpd_access() throws GrokException, IOException {
        Grok g = Grok.create(ResourceManager.PATTERNS, "%{COMMONAPACHELOG}");
        System.out.println("Starting test with nasa log -- may take a while");
        BufferedReader br;
        String line;
        File dir = new File(LOG_DIR_NASA);
        for (File child : dir.listFiles()) {
            br = new BufferedReader(new FileReader(LOG_DIR_NASA + child.getName()));
            while ((line = br.readLine()) != null) {
                //System.out.println(child.getName() + " " +line);
                Match gm = g.match(line);
                gm.captures();
                assertNotNull(gm.toJson());
                assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
            }
            br.close();
        }
    }

}
