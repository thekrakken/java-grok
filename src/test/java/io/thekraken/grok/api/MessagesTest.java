package io.thekraken.grok.api;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import io.thekraken.grok.api.exception.GrokException;

import org.junit.Test;


public class MessagesTest {

  public static  final String LOG_FILE = ResourceManager.MESSAGES;

  @Test
  public void test001_linux_messages() throws GrokException, IOException {
    Grok grok = new Grok();
    grok.addPatternFromFile(ResourceManager.PATTERNS);
    grok.compile("%{MESSAGESLOG}");

    BufferedReader br = new BufferedReader(new FileReader(LOG_FILE));
    String line;
    System.out.println("Starting test with linux messages log -- may take a while");
    while ((line = br.readLine()) != null) {
      Match gm = grok.match(line);
      gm.captures();
      assertNotNull(gm.toJson());
      assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
    }
    br.close();
  }

}
