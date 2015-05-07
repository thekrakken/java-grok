package io.thekraken.grok.api;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApacheDataTypeTest {

  public final static String LOG_FILE = "src/test/resources/access_log";
  public final static String LOG_DIR_NASA = "src/test/resources/nasa/";

  static {
    Locale.setDefault(Locale.ENGLISH);
  }
  
  @Test
  public void test002_httpd_access() throws GrokException, IOException {
    Grok g = Grok.create("patterns/patterns", "%{COMMONAPACHELOG_DATATYPED}");

    BufferedReader br = new BufferedReader(new FileReader(LOG_FILE));
    String line;
    System.out.println("Starting test with httpd log");
    while ((line = br.readLine()) != null) {
      System.out.println(line);
      Match gm = g.match(line);
      gm.captures();
      
      assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());

      Map<String, Object> map = gm.toMap();
      assertTrue(map.get("timestamp") == null || map.get("timestamp") instanceof Date);
      assertTrue(map.get("response") == null || map.get("response") instanceof Integer);
      assertTrue(map.get("ident") == null  || map.get("ident") instanceof Boolean);
      assertTrue(map.get("httpversion") == null || map.get("httpversion") instanceof Float);
      assertTrue(map.get("bytes") == null || map.get("bytes") instanceof Long);
      assertTrue(map.get("verb") == null || map.get("verb") instanceof String);

    }
    br.close();
  }

 }
