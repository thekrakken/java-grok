# Grok

Java Grok is simple API that allows you to easily parse logs and other files (single line). With Java Grok, you can turn unstructured log and event data into structured data (JSON).
Grok is inspired by the logstash inteceptor or filter available [here](http://logstash.net/docs/1.4.1/filters/grok)

-----------------------

### What can I use Grok for?

* reporting errors and other patterns from logs and processes
* parsing complex text output and converting it to json for external processing
* apply 'write-once use-everywhere' to regular expressions
* automatically providing patterns for unknown text inputs (logs you want patterns generated for future matching)

### Maven repository

```maven
<dependency>
  <groupId>de.wellnerbou</groupId>
  <artifactId>java-grok</artifactId>
  <version>0.1.7</version>
</dependency>
```

Or with gradle

```gradle
compile "de.wellnerbou:java-grok:0.1.7"
```

### Usage ([Grok java documentation](http://grok.nflabs.com/javadoc))
Example of how to use java-grok:

```java
    /* Create a new grokCompiler instance */
    GrokCompiler grokCompiler = GrokCompiler.newInstance();
    grokCompiler.registerDefaultPatterns();

    /* Grok pattern to compile, here httpd logs */
    final Grok grok = grokCompiler.compile("%{COMBINEDAPACHELOG}");

    /* Line of log to match */
    String log = "112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"";

    Match gm = grok.match(log);

    /* Get the map with matches */
    final Map<String, Object> capture = gm.capture();
```

### Build Java Grok

Java Grok support Gradle: `./gradlew assemble`
 
### Getting help
Former maintainer: [@anthonycorbacho](https://github.com/anthonycorbacho)

### Thankx to
 * [@anthonycorbacho](https://github.com/anthonycorbacho)
 * [@nokk](https://github.com/nokk)
 * [@wouterdb](https://github.com/wouterdb)
 * [@Leemoonsoo](https://github.com/Leemoonsoo)

**Any contributions are warmly welcome**
