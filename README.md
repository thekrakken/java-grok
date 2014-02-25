<img src="http://peloton.nflabs.com/imgs/logo.png" height="120" align="bottom"/>

Grok is a tool for parsing data (mostly log data)

-----------------------

### Compile

just run maven pakage ``mvn package``
	
find the package in the target folder -> grok-{version}.jar

### Dependencies
 * gson
 * common-lang3
 * named-regex

### Use

	Grok g = new Grok();
	g.addPatternFromFile(/path/to/pattern);
	g.compile("%{URI}");
	Match gm = g.match(yourlog);
	gm.captures();
	//See the result
	System.out.println(gm.toJson());

### Maven repository
Visit [NFLabs Maven repository](https://github.com/NFLabs/mvn-repo)

### Getting help
Maintainer: [Anthony CORBACHO](mailto:corbacho.anthony@gmail.com)

### Thankx to
 * [@wouterdb](https://github.com/wouterdb)
 * [@Leemoonsoo](https://github.com/Leemoonsoo)
