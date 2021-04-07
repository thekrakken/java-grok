package io.krakens.grok.api;

import java.util.HashSet;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

import org.assertj.core.util.Arrays;

public class GrokDocumentationTest {

  @Test
  public void assureCodeInReadmeWorks() {
    /* Create a new grokCompiler instance */
    GrokCompiler grokCompiler = GrokCompiler.newInstance();
    grokCompiler.registerDefaultPatterns();

    /* Grok pattern to compile, here httpd logs */
    final Grok grok = grokCompiler.compile("%{COMBINEDAPACHELOG}");

    /* Line of log to match */
    String log = "112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" "
        + "\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) "
        + "Chrome/25.0.1364.152 Safari/537.22\"";

    Match gm = grok.match(log);

    /* Get the map with matches */
    final Map<String, Object> capture = gm.capture();

    Assertions.assertThat(capture).hasSize(22);
		final boolean debug = false;
		final Object[] keywordArray = new Object[] { "COMBINEDAPACHELOG",
				"COMMONAPACHELOG", "clientip", "ident", "auth", "timestamp", "MONTHDAY",
				"MONTH", "YEAR", "TIME", "HOUR", "MINUTE", "SECOND", "INT", "verb",
				"httpversion", "rawrequest", "request", "response", "bytes", "referrer",
				"agent" };
		if (debug)
			capture.keySet().stream().forEach(System.err::println);
		assertTrue(new HashSet<Object>(Arrays.asList(keywordArray))
				.containsAll(new HashSet<Object>(capture.keySet())));

		Arrays.asList(keywordArray).stream()
				.forEach(o -> assertThat(capture.keySet(), hasItem((String) o)));
		assertThat(new HashSet<Object>(capture.keySet()),
				containsInAnyOrder(keywordArray));
		assertTrue(new HashSet<Object>(capture.keySet())
				.containsAll(new HashSet<Object>(Arrays.asList(keywordArray))));

  }
}
