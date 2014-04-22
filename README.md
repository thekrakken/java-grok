<img src="http://peloton.nflabs.com/imgs/logo.png" height="120" align="bottom"/>

[![Build Status](https://secure.travis-ci.org/thekrakken/java-grok.png?branch=master)](https://travis-ci.org/thekrakken/java-grok)

Java Grok is simple API that allows you to easily parse logs and other files (single line). With Java Grok, you can turn unstructured log and event data into structured data (JSON).


-----------------------

### What can I use Grok for?
* reporting errors and other patterns from logs and processes
* parsing complex text output and converting it to json for external processing
* apply 'write-once use-everywhere' to regular expressions
* automatically providing patterns for unknown text inputs (logs you want patterns generated for future matching)

### Usage ([Grok java documentation](http://grok.nflabs.com/javadoc))
Include Java Grok into your java project and use it like:

        Grok grok = Grok.create("patterns/patterns");
        grok.compile("%{USER}");
        Match gm = grok.match("root");
        gm.captures();
	// See the output
	System.out.println(gm.toJson());

### Maven repository

	<dependency>
	  <groupId>com.nflabs</groupId>
	  <artifactId>grok</artifactId>
	  <version>0.0.5</version>
	</dependency>

Or with gradle
   	
	compile "com.nflabs:grok:0.0.5"

### Build Java Grok

Java Grok support Maven and Gradle
 1. Maven ``mvn package``.
 2. Gradle ``gradle build``, If you dont have gradle installed you can use ``./gradlew build``.	

### Getting help
Maintainer: [@anthonycorbacho](https://github.com/anthonycorbacho)

### Thankx to
 * [@wouterdb](https://github.com/wouterdb)
 * [@Leemoonsoo](https://github.com/Leemoonsoo)
 
**Any contributions are warmly welcome**

