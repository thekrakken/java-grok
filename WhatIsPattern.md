---
layout: page
title: "Create a Grok Pattern"
description: ""
group: nav-left
---
{% include JB/setup %}

## Overview
Java Grok exists to help you do fancier pattern matching with less effort. Grok lets you build (or use existing) sets of named regular expressions and then helps you use them to match strings.

The goal is to bring more semantics to regular expressions and allow you to express ideas rather than syntax. Further, it lets you bring "Don't Repeat Yourself" philosophy to pattern matching, log analysis, etc, by leveraging existing patterns against new data.

As an example, look at this sample log. What do you see?

```bash
Nov  1 21:14:23 scorn kernel: pid 84558 (expect), uid 30206: exited on signal 3
```

In order, your brain reads a timestamp, a hostname, a process or other identifying name, a number, a program name, a uid, and an exit message. You might represent this in words as:

```bash
TIMESTAMP HOST PROGRAM: pid NUMBER (PROGRAM), uid NUMBER: exited on signal NUMBER
```

All of these can be represented by regular expressions. Grok comes with a bunch of pre-defined patterns to make getting started easier, including syslog patterns that help with the above. In grok, this pattern looks like:

```bash
%{SYSLOGBASE} pid %{NUMBER:pid} \(%{WORD:program}\), uid %{NUMBER:uid}: exited on signal %{NUMBER:signal}
```

All of the base grok patterns are in uppercase for style consistency. Each thing in `%{}` is evaluated and replaced with the regular expression it represents.

Given that you may have multiple patterns with the same name, like NUMBER above, you can tag individual patterns with an additional name (like 'pid' or 'program' above) to make later inspection easier.

You will never again have to count parentheses to figure out what capture group number you need.

You can also nest patterns. `SYSLOGBASE` is a good example. `SYSLOGBASE` is actually:

```bash
%{SYSLOGDATE} (?:%{SYSLOGFACILITY} )?%{IPORHOST:logsource} %{SYSLOGPROG}
```

SYSLOGDATE and SYSLOGPROG also have nested patterns:

 * `SYSLOGPROG` = `%{PROG}(?:\[%{PID}\])?`
 * `SYSLOGDATE` = `%{MONTH} +%{MONTHDAY} %{TIME}`
 * `MONTH` = `\b(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch) ...`
 * etc ...

This means anything matching %{SYSLOGDATE} will have all of the nested pattern names accessible as captures. For example, if you wanted to grab the MONTH value from the pattern match, that's trivial.

### What's in a `%{name}`?
#### Any named pattern in your capture
If your pattern was `%{FOO}`, you can access the captured value as `%{FOO}` in your reaction.

If there are multiple `%{FOO}` patterns in your match, the first `%{FOO}` is given in the reaction.

#### Any subname
If your pattern was '%{FOO:bar}' you can access the value with this shorthand '%{bar}' - or you can use the full name '%{FOO:bar}'

## Specifying patterns, or `%{name}`
In grok patterns, you specify a named pattern with %{name}, where 'name' is the name of your pattern. Grok comes with a handful of patterns already defined in the [Grok Pattern](https://raw.githubusercontent.com/NFLabs/java-grok/master/patterns/patterns) file.

### Rename ``%{name}``
With Grok you have the ability to rename patterns with the following syntax `%{name : newName}`

Example

```java
// Get an instance of grok
Grok grok = Grok.EMPTY;

// add a pattern to grok
grok.addPattern("foo", "\\w+");

// compile - rename
grok.compile("%{"foo:bar"}");

// Match with some log
Match m = grok.match("hello");
m.capture();

// Print
System.out.println(m.toMap());
// Then the output should be
// {bar=hello}

```