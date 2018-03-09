package io.thekraken.grok.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestPerformance {
  public static void main(String[] args) throws Exception {
    String input = "98.210.116.51 \"-\" \"-\" [25/Feb/2018:23:10:39 +0000] \"GET /assets/demo-77048f81b565db090ab2f906c9779b5a92629d996e3b77a6680a7136c492a956.png HTTP/1.1\" 304 \"-\"";

    Grok grok = Grok.create("patterns/patterns");
    String pattern = "%{IPORHOST:clientip} \"%{USER:ident}\" \"%{USER:auth}\" \\[%{HTTPDATE:timestamp}\\] \"(?:%{WORD:verb} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion})?|%{DATA:rawrequest})\" %{NUMBER:response} \"(?:%{NUMBER:bytes}|-)\"";
    grok.compile(pattern);

    long start = System.currentTimeMillis();
    for (int i=0; i < 1000000; i++) {
      Match match = grok.match(input);
      match.captures();
      Map<String, Object> map = match.toMap();
    }
    long end = System.currentTimeMillis();
    System.out.println("took: " + (end-start) + " millis");
  }
}
