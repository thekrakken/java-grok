package com.nflabs.grok;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * {@code Grok} parse arbitrary text and structure it
 *
 * @since 0.0.1
 * @author anthonycorbacho
 */
@SuppressWarnings("unused")
public class Grok {

  // Public
  public Map<String, String> patterns;
  public String saved_pattern = null;

  // Private
  private Map<String, String> _captured_map;
  // Extract string like %{Foo} => Foo
  private java.util.regex.Pattern _PATTERN = java.util.regex.Pattern.compile("%\\{(.*?)\\}");
  private Pattern _PATTERN_RE = Pattern.compile("%\\{" + "(?<name>" + "(?<pattern>[A-z0-9]+)"
      + "(?::(?<subname>[A-z0-9_:]+))?" + ")" + "(?:=(?<definition>" + "(?:" + "(?:[^{}]+|\\.+)+"
      + ")+" + ")" + ")?" + "\\}");
  private String _expanded_pattern;
  private String _pattern_origin;
  private Pattern _regexp;
  private Discovery _disco;

  /**
   * Create a new {@code Grok} object
   */
  public Grok() {
    _pattern_origin = null;
    _disco = null;
    _expanded_pattern = null;
    _regexp = null;
    patterns = new TreeMap<String, String>();
    _captured_map = new TreeMap<String, String>();
  }

  /**
   * Add a new pattern to the current {@code Grok} instance
   *
   * @param Pattern Name
   * @param Regular expression
   * @throws GrokException
   **/
  public void addPattern(String name, String pattern) throws GrokException {
    if (name == "" || pattern == "")
      throw new GrokException("Invalid Pattern");
    if (name.isEmpty() || pattern.isEmpty())
      throw new GrokException("Invalid Pattern");
    patterns.put(name, pattern);
  }

  /**
   * Copy the given Map of patterns (pattern name, regular expression) to {@code Grok}, duplicate element
   * will be overwrite
   *
   * @param Map to copy
   * @throws GrokException
   **/
  public void copyPatterns(Map<String, String> cpy) throws GrokException {
    if (cpy == null)
      throw new GrokException("Invalid Patterns");
    if (cpy.isEmpty())
      throw new GrokException("Invalid Patterns");
    for (Map.Entry<String, String> entry : cpy.entrySet())
      patterns.put(entry.getKey().toString(), entry.getValue().toString());
  }

  /**
   * Get the current map of {@code Grok} pattern
   *
   * @return Patterns (name, regular expression)
   */
  public Map<String, String> getPatterns() {
    return this.patterns;
  }

  /**
   * The representation of the regexp in {@code Grok} language
   *
   * @return Regexp in Grok language
   * @see compile
   */
  public String getExpandedPattern() {
    return _expanded_pattern;
  }

  /**
   * Add patterns to {@code Grok} instance from the given file
   *
   * @param Path of the pattern file
   * @throws GrokException
   */
  public void addPatternFromFile(String file) throws GrokException {

    File f = new File(file);
    if (!f.exists())
      throw new GrokException("Pattern not found");
    if (!f.canRead())
      throw new GrokException("Pattern cannot be read");

    FileReader r;
    try {
      r = new FileReader(f);
      addPatternFromReader(r);
      r.close();
    } catch (FileNotFoundException e) {
      throw new GrokException(e.getMessage());
    } catch (IOException e) {
      throw new GrokException(e.getMessage());
    }
  }

  /**
   * Add patterns to {@code Grok} from a Reader
   *
   * @param Reader with {@code Grok} patterns
   * @throws GrokException
   */
  public void addPatternFromReader(Reader r) throws GrokException {
    BufferedReader br = new BufferedReader(r);
    String line;
    // We dont want \n and commented line
    Pattern MY_PATTERN = Pattern.compile("^([A-z0-9_]+)\\s+(.*)$");
    try {
      while ((line = br.readLine()) != null) {
        Matcher m = MY_PATTERN.matcher(line);
        if (m.matches())
          this.addPattern(m.group(1), m.group(2));
      }
      br.close();
    } catch (IOException e) {
      throw new GrokException(e.getMessage());
    } catch (GrokException e) {
      throw new GrokException(e.getMessage());
    }

  }

  /**
   * Match the given <tt>string</tt> with the compiled pattern
   * {@code Grok} will extract data from the string and create an extence of {@code Match}
   *
   * @param Single line of log
   * @return Grok Match
   * @see Match.class
   */
  public Match match(String text) {

    if (_regexp == null)
      return null;

    Matcher m = _regexp.matcher(text);
    Match match = new Match();
    // System.out.println(expanded_pattern);
    if (m.find()) {
      // System.out.println("LLL"+m.group() +" " + m.start(0) +" "+ m.end(0));
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
   * Compile the regexp to {@code Grok} language
   *
   * @param Pattern regex
   * @throws GrokException
   */
  public void compile(String pattern) throws GrokException {
    _expanded_pattern = pattern;
    _pattern_origin = pattern;
    int index = 0;
    Boolean Continue = true;

    // Replace %{foo} with the regex (mostly groupname regex)
    // and then compile the regex
    while (Continue) {
      Continue = false;

      Matcher m = _PATTERN_RE.matcher(_expanded_pattern);
      // Match %{Foo:bar} -> pattern name and subname
      // Match %{Foo=regex} -> add new regex definition
      if (m.find()) {
        Continue = true;
        Map<String, String> group = m.namedGroups();
        if (group.get("definition") != null) {
          try {
            addPattern(group.get("pattern"), group.get("definition"));
            group.put("name", group.get("name") + "=" + group.get("definition"));
          } catch (GrokException e) {
            // Log the exeception
          }
        }
        _captured_map.put("name" + index, (group.get("subname") != null ? group.get("subname")
            : group.get("name")));
        _expanded_pattern =
            StringUtils.replace(_expanded_pattern, "%{" + group.get("name") + "}", "(?<name"
                + index + ">" + this.patterns.get(group.get("pattern")) + ")");
        // System.out.println(_expanded_pattern);
        index++;
      }
    }

    if (_expanded_pattern.isEmpty()) {
      throw new GrokException("Pattern not fount");
    }
    // Compile the regex
    _regexp = Pattern.compile(_expanded_pattern);
  }

  /**
   * {@code Grok} will try to find the best Grok expression that will match your input
   *
   * @param Single line of log
   * @return the Grok pattern
   * @see Discover.class
   */
  public String discover(String input) {

    if (_disco == null)
      _disco = new Discovery(this);
    return _disco.discover(input);
  }

  /**
   * Get the {@code Grok} regexp from his name
   *
   * @param Key
   * @return the value
   */
  public String capture_name(String id) {
    return _captured_map.get(id);
  }

  /**
   *
   * @return getter
   */
  public Map<String, String> getCaptured() {
    return _captured_map;
  }

  /**
   ** Checkers
   **/
  public int isPattern() {
    if (patterns == null)
      return 0;
    if (patterns.isEmpty())
      return 0;
    return 1;
  }
}
