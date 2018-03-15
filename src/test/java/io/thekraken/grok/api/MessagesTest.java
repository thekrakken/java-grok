package io.thekraken.grok.api;

import com.google.common.io.Resources;
import io.thekraken.grok.api.exception.GrokException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


public class MessagesTest {

    public final static String LOG_FILE = ResourceManager.MESSAGES;

    @Test
    public void test001_linux_messages() throws GrokException, IOException {
        GrokCompiler compiler = GrokCompiler.newInstance();
        compiler.register(Resources.getResource(ResourceManager.PATTERNS).openStream());

        Grok g = compiler.compile("%{MESSAGESLOG}");

        BufferedReader br = new BufferedReader(new FileReader(LOG_FILE));
        String line;
        System.out.println("Starting test with linux messages log -- may take a while");
        while ((line = br.readLine()) != null) {
            Match gm = g.match(line);
            Map<String, Object> map = gm.capture();
            assertNotNull(gm.toJson());
            assertNotEquals("{\"Error\":\"Error\"}", gm.toJson());
        }
        br.close();
    }

}
