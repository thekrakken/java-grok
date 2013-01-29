# <img src="http://img703.imageshack.us/img703/4531/logounr.png" height="120" align="bottom"/>
Grok is a tool for parsing data (mostly log data)

-----------------------

### Compile

just run maven pakage

	mvn package
	
find the package in the target folder -> grok-{version}.jar

### Dependencies
+gson
+common-lang3
+named-regex

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
mail: [acorbacho@nflabs.com](mailto:acorbacho@nflabs.com)
[See also](http://www.nflabs.com)

### Info
Grok is a concept of [Jordan Sissel](https://github.com/jordansissel/grok)
