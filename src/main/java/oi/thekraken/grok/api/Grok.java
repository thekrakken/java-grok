/*******************************************************************************
 * Copyright 2014 Anthony Corbacho and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package oi.thekraken.grok.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oi.thekraken.grok.api.exception.GrokException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;


/**
 * {@code Grok} parse arbitrary text and structure it.<p/>
 *
 * {@code Grok} is simple API that allows you to easily parse logs
 * and other files (single line). With {@code Grok},
 * you can turn unstructured log and event data into structured data (JSON).
 *<p/>
 * example:<p/>
 * <pre>
 *  Grok grok = Grok.create("patterns/patterns");
 *  grok.compile("%{USER}");
 *  Match gm = grok.match("root");
 *  gm.captures();
 * </pre>
 *
 * @since 0.0.1
 * @author anthonycorbacho
 */
public class Grok {

  private static final Logger LOG = LoggerFactory.getLogger(Grok.class);
  /**
   * Named regex of the originalGrokPattern.
   */
  private String namedRegex;
  /**
   * Map of the named regex of the originalGrokPattern
   * with id = namedregexid and value = namedregex.
   */
  private Map<String, String> namedRegexCollection;
  /**
   * Original {@code Grok} pattern (expl: %{IP}).
   */
  private String originalGrokPattern;
  /**
   * Pattern of the namedRegex.
   */
  private Pattern compiledNamedRegex;
  /**
   * {@code Grok} discovery.
   */
  private Discovery disco;
  /**
   * {@code Grok} patterns definition.
   */
  private Map<String, String> grokPatternDefinition;

  /** only use in grok discovery. */
  private String savedPattern;

  /**
   * Create Empty {@code Grok}.
   */
  public static final Grok EMPTY = new Grok();

  /**
   * Create a new <i>empty</i>{@code Grok} object.
   */
  public Grok() {
    originalGrokPattern = StringUtils.EMPTY;
    disco = null;
    namedRegex = StringUtils.EMPTY;
    compiledNamedRegex = null;
    grokPatternDefinition = new TreeMap<String, String>();
    namedRegexCollection = new TreeMap<String, String>();
    savedPattern = StringUtils.EMPTY;
  }

  public String getSaved_pattern() {
    return savedPattern;
  }

  public void setSaved_pattern(String savedpattern) {
    this.savedPattern = savedpattern;
  }

  /**
   * Create a {@code Grok} instance with the given patterns file and
   * a {@code Grok} pattern.
   *
   * @param grokPatternPath Path to the pattern file
   * @param grokExpression  - <b>OPTIONAL</b> - Grok pattern to compile ex: %{APACHELOG}
   * @return {@code Grok} instance
   * @throws GrokException
   */
  public static Grok create(String grokPatternPath, String grokExpression)
      throws GrokException {
    if (StringUtils.isBlank(grokPatternPath)) {
      throw new GrokException("{grokPatternPath} should not be empty or null");
    }
    Grok g = new Grok();
    g.addPatternFromFile(grokPatternPath);
    if (StringUtils.isNotBlank(grokExpression)) {
      g.compile(grokExpression);
    }
    return g;
  }

  /**
   * Create a {@code Grok} instance with the given grok patterns file.
   *
   * @param  grokPatternPath : Path to the pattern file
   * @return Grok
   * @throws GrokException
   */
  public static Grok create(String grokPatternPath) throws GrokException {
    return create(grokPatternPath, null);
  }

  /**
   * Add custom pattern to grok in the runtime.
   *
   * @param name : Pattern Name
   * @param pattern : Regular expression Or {@code Grok} pattern
   * @throws GrokException
   **/
  public void addPattern(String name, String pattern) throws GrokException {
    if (StringUtils.isBlank(name)) {
      throw new GrokException("Invalid Pattern name");
    }
    if (StringUtils.isBlank(name)) {
      throw new GrokException("Invalid Pattern");
    }
    grokPatternDefinition.put(name, pattern);
  }

  /**
   * Copy the given Map of patterns (pattern name, regular expression) to {@code Grok},
   * duplicate element will be override.
   *
   * @param cpy : Map to copy
   * @throws GrokException
   **/
  public void copyPatterns(Map<String, String> cpy) throws GrokException {
    if (cpy == null) {
      throw new GrokException("Invalid Patterns");
    }

    if (cpy.isEmpty()) {
      throw new GrokException("Invalid Patterns");
    }
    for (Map.Entry<String, String> entry : cpy.entrySet()) {
      grokPatternDefinition.put(entry.getKey().toString(), entry.getValue().toString());
    }
  }

  /**
   * Get the current map of {@code Grok} pattern.
   *
   * @return Patterns (name, regular expression)
   */
  public Map<String, String> getPatterns() {
    return grokPatternDefinition;
  }

  /**
   * Get the named regex from the {@code Grok} pattern. <p></p>
   * See {@link #compile(String)} for more detail.
   * @return named regex
   */
  public String getNamedRegex() {
    return namedRegex;
  }

  /**
   * Add patterns to {@code Grok} from the given file.
   *
   * @param file : Path of the grok pattern
   * @throws GrokException
   */
  public void addPatternFromFile(String file) throws GrokException {

    File f = new File(file);
    if (!f.exists()) {
      throw new GrokException("Pattern not found");
    }

    if (!f.canRead()) {
      throw new GrokException("Pattern cannot be read");
    }

    FileReader r = null;
    try {
      r = new FileReader(f);
      addPatternFromReader(r);
    } catch (FileNotFoundException e) {
      throw new GrokException(e.getMessage());
    } catch (@SuppressWarnings("hiding") IOException e) {
      throw new GrokException(e.getMessage());
    } finally {
      try {
        if (r != null) {
          r.close();
        }
      } catch (IOException io) {
        // TODO(anthony) : log the error
      }
    }
  }

  /**
   * Add patterns to {@code Grok} from a Reader.
   *
   * @param r : Reader with {@code Grok} patterns
   * @throws GrokException
   */
  public void addPatternFromReader(Reader r) throws GrokException {
    BufferedReader br = new BufferedReader(r);
    String line;
    // We dont want \n and commented line
    Pattern pattern = Pattern.compile("^([A-z0-9_]+)\\s+(.*)$");
    try {
      while ((line = br.readLine()) != null) {
        Matcher m = pattern.matcher(line);
        if (m.matches()) {
          this.addPattern(m.group(1), m.group(2));
        }
      }
      br.close();
    } catch (IOException e) {
      throw new GrokException(e.getMessage());
    } catch (GrokException e) {
      throw new GrokException(e.getMessage());
    }

  }

  /**
   * Match the given <tt>log</tt> with the named regex.
   * And return the json representation of the matched element
   *
   * @param log : log to match
   * @return json representation og the log
   */
  public String capture(String log){
    Match match = match(log);
    match.captures();
    return match.toJson();
  }

  /**
   * Match the given list of <tt>log</tt> with the named regex
   * and return the list of json representation of the matched elements.
   *
   * @param logs : list of log
   * @return list of json representation of the log
   */
  public List<String> captures(List<String> logs){
    List<String> matched = new ArrayList<String>();
    for (String log : logs) {
      Match match = match(log);
      match.captures();
      matched.add(match.toJson());
    }
    return matched;
  }

  /**
   * Match the given <tt>text</tt> with the named regex
   * {@code Grok} will extract data from the string and get an extence of {@link Match}.
   *
   * @param text : Single line of log
   * @return Grok Match
   */
  public Match match(String text) {
    if (compiledNamedRegex == null || StringUtils.isBlank(text)) {
      return Match.EMPTY;
    }

    Matcher m = compiledNamedRegex.matcher(text);
    Match match = new Match();
    if (m.find()) {
      match.setSubject(text);
      match.setGrok(this);
      match.setMatch(m);
      match.setStart(m.start(0));
      match.setEnd(m.end(0));
    }
    return match;
  }

  /**
   * Compile the {@code Grok} pattern to named regex pattern.
   *
   * @param pattern : Grok pattern (ex: %{IP})
   * @throws GrokException
   */
  public void compile(String pattern) throws GrokException {

    if (StringUtils.isBlank(pattern)) {
      throw new GrokException("{pattern} should not be empty or null");
    }

    namedRegex = pattern;
    originalGrokPattern = pattern;
    int index = 0;
    /** flag for infinite recurtion */
    int iterationLeft = 1000;
    Boolean continueIteration = true;

    // Replace %{foo} with the regex (mostly groupname regex)
    // and then compile the regex
    while (continueIteration) {
      continueIteration = false;
      if (iterationLeft <= 0) {
        throw new GrokException("Deep recursion pattern compilation of " + originalGrokPattern);
      }
      iterationLeft--;

      Matcher m = GrokUtils.GROK_PATTERN.matcher(namedRegex);
      // Match %{Foo:bar} -> pattern name and subname
      // Match %{Foo=regex} -> add new regex definition
      if (m.find()) {
        continueIteration = true;
        Map<String, String> group = m.namedGroups();
        if (group.get("definition") != null) {
          try {
            addPattern(group.get("pattern"), group.get("definition"));
            group.put("name", group.get("name") + "=" + group.get("definition"));
          } catch (GrokException e) {
            // Log the exeception
          }
        }
        if(!grokPatternDefinition.containsKey(group.get("pattern"))){
        	throw new GrokException("Pattern name " + group.get("pattern") + " unknown!");
        }
        namedRegexCollection.put("name" + index,
            (group.get("subname") != null ? group.get("subname") : group.get("name")));
        namedRegex =
            StringUtils.replace(namedRegex, "%{" + group.get("name") + "}", "(?<name" + index + ">"
                + grokPatternDefinition.get(group.get("pattern")) + ")");
        // System.out.println(_expanded_pattern);
        index++;
      }
    }

    if (namedRegex.isEmpty()) {
      throw new GrokException("Pattern not fount");
    }
    // Compile the regex
    compiledNamedRegex = Pattern.compile(namedRegex);
  }

  /**
   * {@code Grok} will try to find the best expression that will match your input.
   * {@link Discovery}
   *
   * @param input : Single line of log
   * @return the Grok pattern
   */
  public String discover(String input) {

    if (disco == null) {
      disco = new Discovery(this);
    }
    return disco.discover(input);
  }

  /**
   * Original grok pattern used to compile to the named regex.
   *
   * @return String Original Grok pattern
   */
  public String getOriginalGrokPattern(){
    return originalGrokPattern;
  }

  /**
   * Get the named regex from the given id.
   *
   * @param id : named regex id
   * @return String of the named regex
   */
  public String getNamedRegexCollectionById(String id) {
    return namedRegexCollection.get(id);
  }

  /**
   * Get the full collection of the named regex.
   *
   * @return named RegexCollection
   */
  public Map<String, String> getNamedRegexCollection() {
    return namedRegexCollection;
  }
}
