# Java Grok Project

Grok is a tool for parsing data (mostly log data)

-----------------------

### USE

	Grok g = new Grok();
	g.addPatternFromFile(/path/to/pattern);
	g.compile("%{URI}");
	Match gm = g.match(yourlog);
	gm.captures();
	//See the result
	System.out.println(gm.toJson());

### Getting help
mail: acorbacho@nflabs.com

### Info
Grok is originally developed in C by [Jordan Sissel](https://github.com/jordansissel/grok)

