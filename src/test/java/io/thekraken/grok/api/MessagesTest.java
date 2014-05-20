package io.thekraken.grok.api;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

import org.junit.Test;


public class MessagesTest {

  public final static String LOG_FILE = "src/test/resources/message/messages";

  @Test
  public void test001_linux_messages() throws GrokException, IOException {
    Grok g = new Grok();
    g.addPatternFromFile("patterns/patterns");
    g.compile("%{MESSAGESLOG}");

    BufferedReader br = new BufferedReader(new FileReader(LOG_FILE));
    String line;
    System.out.println("Starting test with linux messages log -- may take a while");
    while ((line = br.readLine()) != null) {
      Match gm = g.match(line);
      gm.captures();
      assertNotNull(gm.toJson());
      assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
    }
    br.close();
  }

}
