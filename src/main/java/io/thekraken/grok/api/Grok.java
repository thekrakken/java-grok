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
package io.thekraken.grok.api;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * {@code Grok} parse arbitrary text and structure it.<br>
 *
 * {@code Grok} is simple API that allows you to easily parse logs
 * and other files (single line). With {@code Grok},
 * you can turn unstructured log and event data into structured data (JSON).
 *<br>
 * example:<br>
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
  /**
   * Named regex of the originalGrokPattern.
   */
  private final String namedRegex;
  /**
   * Map of the named regex of the originalGrokPattern
   * with id = namedregexid and value = namedregex.
   */
  private final Map<String, String> namedRegexCollection;
  /**
   * Original {@code Grok} pattern (expl: %{IP}).
   */
  private final String originalGrokPattern;
  /**
   * Pattern of the namedRegex.
   */
  private final Pattern compiledNamedRegex;

  /**
   * {@code Grok} patterns definition.
   */
  private final Map<String, String> grokPatternDefinition;

  public final Set<String> namedGroups;

  public final Map<String, IConverter> converters;

  /**
   * {@code Grok} discovery.
   */
  private Discovery disco;

  /** only use in grok discovery. */
  private String savedPattern = "";

  public Grok(String pattern,
              String namedRegex,
              Pattern compiledNamedRegex,
              Map<String, String> namedRegexCollection,
              Set<String> namedGroups,
              Map<String, IConverter> converters,
              Map<String, String> patternDefinitions) {
    this.originalGrokPattern = pattern;
    this.namedRegex = namedRegex;
    this.compiledNamedRegex = compiledNamedRegex;
    this.namedRegexCollection = namedRegexCollection;
    this.namedGroups = namedGroups;
    this.converters = converters;
    this.grokPatternDefinition = patternDefinitions;
  }

  public String getSaved_pattern() {
    return savedPattern;
  }

  public void setSaved_pattern(String savedpattern) {
    this.savedPattern = savedpattern;
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
   * Get the named regex from the {@code Grok} pattern. <br>
   * @return named regex
   */
  public String getNamedRegex() {
    return namedRegex;
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

  /**
   * Match the given <tt>log</tt> with the named regex.
   * And return the json representation of the matched element
   *
   * @param log : log to match
   * @return json representation og the log
   */
  public String capture(String log){
    Match match = match(log);
    match.capture();
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
      match.capture();
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
    if (m.find()) {
      return new Match(
          text, this, m, m.start(0), m.end(0)
      );
    }

    return Match.EMPTY;
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
}
