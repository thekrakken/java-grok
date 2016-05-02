# Grok
[![Build Status](https://secure.travis-ci.org/thekrakken/java-grok.png?branch=master)](https://travis-ci.org/thekrakken/java-grok)

Java Grok is simple API that allows you to easily parse logs and other files (single line). With Java Grok, you can turn unstructured log and event data into structured data (JSON).


-----------------------

### What can I use Grok for?
* reporting errors and other patterns from logs and processes
* parsing complex text output and converting it to json for external processing
* apply 'write-once use-everywhere' to regular expressions
* automatically providing patterns for unknown text inputs (logs you want patterns generated for future matching)

### Maven repository

```maven
<dependency>
  <groupId>io.thekraken</groupId>
  <artifactId>grok</artifactId>
  <version>0.1.4</version>
</dependency>
```

Or with gradle

```gradle
compile "io.thekraken:grok:0.1.4"
```

### Usage ([Grok java documentation](http://grok.nflabs.com/javadoc))
Example of how to use java-grok:

```java
/** Create a new grok instance */
Grok grok = Grok.create("patterns/patterns");

/** Grok pattern to compile, here httpd logs */
grok.compile("%{COMBINEDAPACHELOG}");

/** Line of log to match */
String log =
"112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\""

Match gm = grok.match(log);
gm.captures();

/** Get the output */
System.out.println(gm.toJson());
```

### Build Java Grok

Java Grok support Gradle: ``./gradlew build``
 
### Getting help
Maintainer: [@anthonycorbacho](https://github.com/anthonycorbacho)

### Thankx to
 * [@nokk](https://github.com/nokk)
 * [@wouterdb](https://github.com/wouterdb)
 * [@Leemoonsoo](https://github.com/Leemoonsoo)

**Any contributions are warmly welcome**

Grok is inspired by the logstash inteceptor or filter available [here](http://logstash.net/docs/1.4.1/filters/grok)
