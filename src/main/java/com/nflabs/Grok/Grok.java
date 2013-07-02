package com.nflabs.Grok;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import com.nflabs.Grok.GrokError;
import com.nflabs.Grok.Match;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public class Grok extends Object {
	
	//Public
	public Map< String,String > 			patterns;
	public String 							saved_pattern = null;
	
	//Private
	private Map< String,String > 			_captured_map;
	// manage string like %{Foo} => Foo
	private java.util.regex.Pattern 		_PATTERN = java.util.regex.Pattern.compile("%\\{(.*?)\\}");
	private Pattern 						_PATTERN_RE = Pattern.compile("%\\{" +
																			"(?<name>"+
																				"(?<pattern>[A-z0-9]+)"+
																					"(?::(?<subname>[A-z0-9_:]+))?"+
																			")"+
																			"(?:=(?<definition>"+
																					"(?:"+
																						"(?:[^{}]+|\\.+)+"+
																					")+" +
																				 ")" +
																			")?"+
																			"\\}");
	private String 							_expanded_pattern;
	private String 							_pattern_origin;
	private Pattern 						_regexp;
	private Discovery 						_disco;
	
	/**
	 ** Constructor.
	 **/
	public		Grok(){	
		
		_pattern_origin = null;
		_disco = null;
		_expanded_pattern = null;
		_regexp = null;
		patterns = new TreeMap<String, String>();
		_captured_map = new TreeMap<String, String>();
	}
	
	/**
	 * Add a new pattern
	 * 
	 *  @param name Name of the pattern
	 *  @param pattern regex string
	 **/
	public int addPattern( String name, String pattern){
		if( name.isEmpty() || pattern.isEmpty() )
			return GrokError.GROK_ERROR_UNINITIALIZED;
		patterns.put(name, pattern);
		return GrokError.GROK_OK;
	}
	
	/**
	 * Copy the map patterns into the grok pattern
	 * 
	 * @param Map of the pattern to copy
	 **/
	public int copyPatterns( Map<String, String> cpy ){
		if( cpy.isEmpty() || cpy == null)
			return GrokError.GROK_ERROR_UNINITIALIZED;
		for (Map.Entry<String, String> entry : cpy.entrySet())
	        patterns.put(entry.getKey().toString(), entry.getValue().toString());
		return GrokError.GROK_OK;
	}
	
	/**
	 * @return the current map grok patterns
	 */
	public Map< String,String > getPatterns(){
		return this.patterns;
	}
	
	/**
	 * @return the compiled regex of <tt>expanded_pattern</tt>
	 * @see compile
	 */
	public Pattern getRegEx(){
		return _regexp;
	}
	
	/**
	 * 
	 * @return the string pattern
	 * @see compile
	 */
	public String getExpandedPattern(){
		return _expanded_pattern;
	}
	
	/**
	 * Add patterns to grok from a file
	 * 
	 * @param file that contains the grok patterns
	 * @throws Throwable
	 */
	public int addPatternFromFile( String file) throws Throwable{
		
		File f = new File(file);
		if(!f.exists())
			return GrokError.GROK_ERROR_FILE_NOT_ACCESSIBLE;
		if( !f.canRead() )
			return GrokError.GROK_ERROR_FILE_NOT_ACCESSIBLE;
		
		return addPatternFromReader(new FileReader(f));
	}
	
	/**
	 * Add patterns to grok from a reader
	 * 
	 * @param reader that contains the grok patterns
	 * @throws Throwable
	 */
	public int addPatternFromReader(Reader r) throws Throwable{
		BufferedReader br = new BufferedReader(r);
        String line;
        //We dont want \n and commented line
        Pattern MY_PATTERN = Pattern.compile("^([A-z0-9_]+)\\s+(.*)$");
        while((line = br.readLine()) != null) {
        	Matcher m = MY_PATTERN.matcher(line);
        	if( m.matches() )
        		this.addPattern( m.group(1), m.group(2) );
        }
        br.close();
        return GrokError.GROK_OK;
	}
	
	/**
	 * Match the <tt>text</tt> with the pattern
	 * 
	 * @param text to match
	 * @return Grok Match
	 * @see Match
	 */
	public Match match( String text ){
		
		if( _regexp == null)
			return null;
		
		Matcher m = _regexp.matcher(text);
		Match match = new Match();
		//System.out.println(expanded_pattern);
		if( m.find() )
		{		
			//System.out.println("LLL"+m.group() +" " + m.start(0) +" "+ m.end(0));
			match.setSubject(text);
			match.grok = this;
			match.match = m;
			match.start = m.start(0);
			match.end = m.end(0);
			match.line = text;
			return match;
		}
		return match;
	}
	
	/**
	 * Transform grok regex into a compiled regex
	 * 
	 * @param Grok pattern regex
	 */
	public int compile( String pattern ){
		_expanded_pattern = new String( pattern );
		_pattern_origin = new String( pattern );
		int index = 0;
		Boolean Continue = true;
		
		//Replace %{foo} with the regex (mostly groupname regex) 
		//and then compile the regex
		while (Continue){
			Continue=false;
			
			Matcher m = _PATTERN_RE.matcher(_expanded_pattern);
			// Match %{Foo:bar} -> pattern name and subname
			// Match %{Foo=regex} -> add new regex definition
			if (m.find() ){
				Continue = true;
				Map<String, String> group = m.namedGroups();

				if(group.get("definition") != null){
					addPattern(group.get("pattern"), group.get("definition"));
					group.put("name", group.get("name") +"="+ group.get("definition") );
					//System.out.println("%{"+group.get("name")+"} =>" + this.patterns.get(group.get("pattern")));
				}
				_captured_map.put( "name"+index, (group.get("subname") != null ? group.get("subname"):group.get("name")));
				_expanded_pattern = StringUtils.replace(_expanded_pattern, "%{"+group.get("name")+"}", "(?<name"+index+">" + this.patterns.get(group.get("pattern"))+")");
				//System.out.println(_expanded_pattern);
				index++;
			}			
		}
		//System.out.println(_captured_map);
		//Compile the regex
		if(!_expanded_pattern.isEmpty()){
			_regexp = Pattern.compile(_expanded_pattern);
			return GrokError.GROK_OK;
		}
		return GrokError.GROK_ERROR_PATTERN_NOT_FOUND;
	}
	
	/**
	 * Grok can find the pattern
	 * 
	 * @param input the file to analyze
	 * @return the grok pattern
	 */
	public String discover( String input ){
		
		if (_disco == null )
			_disco = new Discovery( this );
		return _disco.discover(input);
	}
	
	/**
	 * 
	 * @param Key
	 * @return the value
	 */
	public String capture_name( String id ){
		return _captured_map.get(id);
	}

	/**
	 * 
	 * @return getter
	 */
	public Map<String, String> getCaptured(){
		return _captured_map;
	}
	
	/**
	 ** Checkers 
	 **/
	public int isPattern(){
		if( patterns == null )
			return 0;
		if(patterns.isEmpty())
			return 0;
		return 1;
	}
}
