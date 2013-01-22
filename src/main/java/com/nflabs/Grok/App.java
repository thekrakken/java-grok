package com.nflabs.Grok;


public class App 
{
    public static void main( String[] args )
    {
    	Grok g = new Grok();
		try {
			
			//patterns
			g.addPatternFromFile("patterns/base");
			
			//String log = "http://www.google.com/trololo?=lol&oui and 00:de:ad:be:ef:00 on 192.168.0.12 with 'Something Nice in there'";
			//String log = "66.186.236.233 - - [22/Jan/2006:07:35:42 -0500] 'GET / HTTP/1.1' 403 3931 '-' 'Mozilla/4.0 (compatible; MSIE 5.5; Windows 98)'";
			//String log ="192.168.0.12 -<> 22/Jan/2006:07:35:42 -0500 http://www.google.fr";
			//String pattern = g.discover(log);
			//System.out.println(pattern);
			//g.compile(pattern);
			//Match gm = g.match(log);
			//gm.captures();
			//System.out.println(gm.toJson());
			
			//g.compile("%{IP:address} - - \\[%{HTTPDATE:timestamp}\\] %{QUOTEDSTRING:request} %{NUMBER:response} (?:%{NUMBER:bytes}|-) %{QUOTEDSTRING:Referer} %{QUOTEDSTRING:Agent}");
			//Match gm;
			//what we are looking for?
			g.compile("%{URI}");
			//line ex
			Match gm = g.match(/*"2001:0db8:85a3:0042:1000:8a2e:0370:7334"*/ "http://www.google.com/search=lol");
			gm.captures();
			gm.garbage.addToRemove("COMMONMAC");
			gm.garbage.addFromListRemove(null);
			System.out.println(gm.toJson());
			
			
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
