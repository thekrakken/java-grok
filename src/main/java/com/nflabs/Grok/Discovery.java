package com.nflabs.Grok;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;

public class Discovery {
	
	private				Grok _grok;
	
	/**
	 ** Constructor
	 **/
	public	Discovery( Grok grok ){
		_grok = grok;
	}
	
	/**
	 * Sort by regex complexity
	 * 
	 * @param Map of the pattern name and grok instance
	 * @return the map sorted by grok pattern complexity
	 */
	private Map< String, Grok > Sort(Map< String, Grok > groks){
		
		List< Grok > groky  = new ArrayList< Grok >(groks.values() );
		Map< String, Grok > mGrok = new LinkedHashMap< String, Grok >();
		Collections.sort( groky, new Comparator<Grok>(){
			
			public int compare(Grok g1, Grok g2){
				return (this.complexity(g1.getExpandedPattern()) < this.complexity(g2.getExpandedPattern())) ? 1 : 0;
			}

			private int complexity(String expandedPattern) {
				int score = 0;
				score += expandedPattern.split("\\Q"+"|"+"\\E", -1).length - 1;
				score += expandedPattern.length();
				return score;
			}
		});
		
		for ( Grok g : groky ){
			mGrok.put(g.saved_pattern, g);
		}
		return mGrok;
		
	}

	/**
	 * 
	 * @param regex string
	 * @return the complexity of the regex
	 */
	private int complexity(String expandedPattern) {
		int score = 0;
		
		score += expandedPattern.split("\\Q"+"|"+"\\E", -1).length - 1;
		score += expandedPattern.length();
		
		return score;
	}
	
	/**
	 * 
	 * @param Source string where we want to find the Grok pattern
	 * @return Grok pattern %{Foo}...
	 */
	public String discover( String text)
	{
		if( text == null ) return "";
		
		Map< String, Grok > groks = new TreeMap<String, Grok>();
		Map< String,String > gPatterns = _grok.getPatterns();	
		//Boolean done = false;
		String texte = new String(text);
		
		//Compile the pattern
		Iterator<Entry<String, String>> it = gPatterns.entrySet().iterator();
	    while (it.hasNext()) {
	    	@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	    	String key = pairs.getKey().toString();
	    	Grok g = new Grok();
	    	
	    	//g.patterns.putAll( gPatterns );
	    	g.copyPatterns(gPatterns);
	    	g.saved_pattern = key;
	    	g.compile("%{" + key +"}");
	    	groks.put(key, g);
	    }
	    
	    //Sort patterns by complexity
	    Map< String, Grok > patterns = this.Sort(groks);
	    
	   // while (!done){
	   // 	done = true;
	    	Iterator<Entry<String, Grok>> pit = patterns.entrySet().iterator();
		    while (pit.hasNext()) {
		    	@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)pit.next();
		    	String key = pairs.getKey().toString();
		    	Grok value = (Grok) pairs.getValue();
		    	
		    	//We want to search with more complex pattern
		    	//We avoid word, small number, space....
		    	if( this.complexity(value.getExpandedPattern()) < 20)
		    		continue;
		    	
		    	Match m = value.match(text);		    	
		    	if( m.isNull() )
		    		continue;
		    	//get the part of the matched text
		    	String part = getPart(m , text);
		    	
		    	//we skip boundary word
		    	Pattern MY_PATTERN = Pattern.compile(".\\b.");
		    	Matcher ma = MY_PATTERN.matcher(part);
		    	if( !ma.find() )
		    		continue;
		    	
		    	//We skip the part that already include %{Foo}
		    	Pattern MY_PATTERN2 = Pattern.compile("%\\{[^}+]\\}");
		    	Matcher ma2 = MY_PATTERN2.matcher(part);
		    	
		    	if( ma2.find() )
		    		continue;
		    	texte = StringUtils.replace(texte, part, "%{"+key+"}");
		    }
	    //}
	    
		return texte;
	}
	
	/**
	 * Get the substring tht match with the text
	 * 
	 * @param Grok Match
	 * @param texte
	 * @return string
	 */
	private String getPart(Match m, String text ){

		if( m == null || text == null ) return "";
		
		return text.substring(m.start, m.end);
	}
}
