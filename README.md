<img src="http://peloton.nflabs.com/imgs/logo.png" height="120" align="bottom"/>

Java Grok is simple tool that allows you to easily parse logs and other files (single line). With Java Grok, you can turn unstructured log and event data into structured data (JSON).

-----------------------

### Build Java Grok

Java Grok support Maven and Gradle
 1. Maven ``mvn package``.
 2. Gradle ``gradle build``, If you dont have gradle installed you can use ``./gradlew build``.

### Usage
Include Java Grok into your java project and use it like:

	Grok g = new Grok();
	g.addPatternFromFile(/path/to/pattern);
	g.compile("%{URI}");
	Match gm = g.match(yourlog);
	gm.captures();
	//See the result
	System.out.println(gm.toJson());

### Maven repository

	<dependency>
	  <groupId>com.nflabs</groupId>
	  <artifactId>grok</artifactId>
	  <version>0.0.4</version>
	</dependency>
	

### Getting help
Maintainer: [@anthonycorbacho](https://github.com/anthonycorbacho)

### Thankx to
 * [@wouterdb](https://github.com/wouterdb)
 * [@Leemoonsoo](https://github.com/Leemoonsoo)
 
**Any contributions are warmly welcome**

