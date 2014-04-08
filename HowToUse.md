---
layout: page
title: "How to use"
subtitle : ""
description: ""
group: nav-left
---
{% include JB/setup %}

### How to use Grok in your project?
Include Java Grok into your java project and use it like:

```java
Grok g = new Grok();
g.addPatternFromFile(/path/to/pattern);
g.compile("%{URI}");
Match gm = g.match(yourlog);
gm.captures();
//See the result
System.out.println(gm.toJson());
```

### Maven repository
For **MAVEN** project: Add to your `pom.xml`

```java
<dependency>
  <groupId>com.nflabs</groupId>
  <artifactId>grok</artifactId>
  <version>0.0.5</version>
</dependency>
```

For **GRADLE** project: Add to your `build.gradle`

```java
  compile "com.nflabs:grok:0.0.5"
```

### Build Java Grok

Java Grok support Maven and Gradle compilation
 
 1. Maven ``mvn package``.
 2. Gradle ``gradle assemble``, If you dont have gradle installed you can use ``./gradlew build``.	
