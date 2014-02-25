package com.nflabs.grok;


import com.google.code.regexp.Matcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Match {


	public Grok grok;	//current grok instance
	public Matcher match;	//regex matcher
	public int start; 	//offset
	public int end;	//offset end
	public String line;	//source
	public	Garbage garbage;

	private String _subject;	//texte
	private Map<String, Object> _capture;

	/**
	 ** Contructor
	 **/
	public Match() {
		_subject = "Nothing";
		grok = null;
		match = null;
		_capture = new TreeMap<String, Object>();
		garbage = new Garbage();
		start = 0;
		end = 0;
	}


	/**
	 *
	 * @param line to analyze / save
	 * @return
	 */
	public void	setSubject( String text ) {
		if( text == null ) return;
		if( text.isEmpty() ) return;
		_subject = text;
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
	public void captures(){
		if( this.match == null ) return;

		//_capture.put("LINE", this.line);
		//_capture.put("LENGTH", this.line.length() +"");

		Map<String, String> mappedw = this.match.namedGroups();
		Iterator<Entry<String, String>> it = mappedw.entrySet().iterator();
	    while (it.hasNext()) {

			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        String key = null;
	        Object value = null;
	        if ( !this.grok.capture_name(pairs.getKey().toString()).isEmpty() )
	        	key = this.grok.capture_name(pairs.getKey().toString());
	        if( pairs.getValue() != null ){
	        	value = pairs.getValue().toString();
	        	if( this.isInteger( value.toString() ) )
	        		value = Integer.parseInt( value.toString() );
	        	else
	        		value = cleanString(pairs.getValue().toString());
	        }

	        _capture.put( key  , (Object)value);
	        it.remove(); // avoids a ConcurrentModificationException
	    }
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
		Gson gs = new GsonBuilder().setPrettyPrinting().create();;//new Gson();
		return gs.toJson(/*cleanMap(*/_capture/*)*/);

	}

	/**
	 *
	 * @return java map object from the matched element in the text
	 */
	public Map<String, Object> toMap(){
		this.cleanMap();
		return _capture;
	}

	/**
	 * remove and/or rename items
	 */
	private void cleanMap(){
		garbage.rename(_capture);
		garbage.remove(_capture);
	}

	/**
	 */
	public Boolean isNull(){
		if( this.match == null )
			return true;
		return false;
	}

	private boolean isInteger(String s) {
	    try {
	        Integer.parseInt(s);
	    } catch(NumberFormatException e) {
	        return false;
	    }
	    return true;
	}
}
