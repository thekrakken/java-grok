package com.nflabs.Grok;


import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.TreeMap;

import com.google.code.regexp.Matcher;
import com.google.gson.Gson;
import com.nflabs.Grok.Grok;

public class Match {
	
	public Grok 					grok;	//current grok instance
	public Matcher 					match;	//regex matcher
	public int 						start; 	//offset
	public int 						end;	//offset end
	public	Garbage					garbage;
	
	private String 					_subject;	//texte
	private Map<String, String> 	_capture;
	
	/**
	 ** Contructor 
	 **/
	public Match(){
		_subject = "Nothing";
		grok = null;
		match = null;
		_capture = new TreeMap<String, String>();
		garbage = new Garbage();
		start = 0;
		end = 0;
	}
	
	
	/**
	 * 
	 * @param line to analyze / save
	 * @return
	 */
	public int	setSubject( String text ) {
		if( text == null ) return GrokError.GROK_ERROR_UNINITIALIZED;
		if( text.isEmpty() )
			return GrokError.GROK_ERROR_UNINITIALIZED;
		_subject = text;
		return GrokError.GROK_OK;
	}
	
	/**
	 * Getter
	 * @return the subject
	 */
	public String getSubject(){
		return _subject;
	}
	
	/**
	 * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map
	 * 
	 * @see getSubject
	 * @see toJson
	 * @return Grok success
	 */
	public int captures(){
		if( this.match == null )
			return GrokError.GROK_ERROR_UNINITIALIZED;
		
		Map<String, String> mappedw = this.match.namedGroups();
		//System.out.println(mappedw);
		Iterator<Entry<String, String>> it = mappedw.entrySet().iterator();
	    while (it.hasNext()) {
	       
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        String key = null;
	        String value = null;
	        if ( !this.grok.capture_name(pairs.getKey().toString()).isEmpty() )
	        	key = this.grok.capture_name(pairs.getKey().toString());
	        if( pairs.getValue() != null )
	        	value = cleanString(pairs.getValue().toString());
	        _capture.put( key  , value);
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    return GrokError.GROK_OK;
	}
	
	
	/**
	 * remove from the string the quote and dquote
	 * 
	 * @param string to pure: "my/text"
	 * @return unquoted string: my/text
	 */
	private String cleanString( String value ){
		if( value == null || value.isEmpty() )
			return value;
		char[] tmp = value.toCharArray();
    	if( (tmp[0] == '"' && tmp[value.length()-1] == '"')
    		|| (tmp[0] == '\'' && tmp[value.length()-1] == '\''))
    		value = value.substring(1, value.length()-1);
    	return value;
	}
	
	
	/**
	 * 
	 * @return Json file from the matched element in the text
	 * @see google json
	 */
	public String toJson(){
		if( _capture == null ) return "{\"Error\":\"Error\"}";
		if( _capture.isEmpty() )
			return null;
		
		this.cleanMap();
		Gson gs = new Gson();		
		return gs.toJson(/*cleanMap(*/_capture/*)*/);
		
	}
	
	/**
	 * 
	 * @return java map object from the matched element in the text
	 */
	public Map<String, String> toMap(){
		return _capture;
	}
	
	/**
	 * 
	 */
	private void cleanMap(){
		garbage.remove(_capture);
		garbage.rename(_capture);
	}
	
	
	public Boolean isNull(){
		if( this.match == null )
			return true;
		return false;
	}
	
}
