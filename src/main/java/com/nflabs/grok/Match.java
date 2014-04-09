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
package com.nflabs.grok;


import com.google.code.regexp.Matcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * {@code Match} is a representation in {@code Grok} world of your log
 *
 * @author anthonycorbacho
 * @since 0.0.1
 */
public class Match {


  public Grok grok; // current grok instance
  public Matcher match; // regex matcher
  public int start; // offset
  public int end; // offset end
  public String line; // source
  public Garbage garbage;

  private String _subject; // texte
  private Map<String, Object> _capture;

  /**
   *Create a new {@code Match} object
   */
  private Match() {
    _subject = "Nothing";
    grok = null;
    match = null;
    _capture = new TreeMap<String, Object>();
    garbage = new Garbage();
    start = 0;
    end = 0;
  }

  /**
   * Singleton
   *
   * @return instance of Match
   */
  public static Match getInstance() {
    return MatchHolder.INSTANCE;
  }

  private static class MatchHolder {
    private static final Match INSTANCE = new Match();
  }


  /**
   *  Set the single line of log to parse
   *
   * @param single line of log
   * @return
   */
  public void setSubject(String text) {
    if (text == null)
      return;
    if (text.isEmpty())
      return;
    _subject = text;
  }

  /**
   * Retrurn the single line of log
   *
   * @return the single line of log
   */
  public String getSubject() {
    return _subject;
  }

  /**
   * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map
   *
   * @see getSubject
   * @see toJson
   * @return Grok success
   */
  public void captures() {
    if (this.match == null)
      return;
    _capture.clear();

    // _capture.put("LINE", this.line);
    // _capture.put("LENGTH", this.line.length() +"");

    Map<String, String> mappedw = this.match.namedGroups();
    Iterator<Entry<String, String>> it = mappedw.entrySet().iterator();
    while (it.hasNext()) {

      @SuppressWarnings("rawtypes")
      Map.Entry pairs = (Map.Entry) it.next();
      String key = null;
      Object value = null;
      if (this.grok.capture_name(pairs.getKey().toString()) == null) {
        key = pairs.getKey().toString();
      } else if (!this.grok.capture_name(pairs.getKey().toString()).isEmpty()) {
        key = this.grok.capture_name(pairs.getKey().toString());
      }
      if (pairs.getValue() != null) {
        value = pairs.getValue().toString();
        if (this.isInteger(value.toString()))
          value = Integer.parseInt(value.toString());
        else
          value = cleanString(pairs.getValue().toString());
      }

      _capture.put(key, (Object) value);
      it.remove(); // avoids a ConcurrentModificationException
    }
  }


  /**
   * remove from the string the quote and double quote
   *
   * @param string to pure: "my/text"
   * @return unquoted string: my/text
   */
  private String cleanString(String value) {
    if (value == null || value.isEmpty())
      return value;
    char[] tmp = value.toCharArray();
    if ((tmp[0] == '"' && tmp[value.length() - 1] == '"')
        || (tmp[0] == '\'' && tmp[value.length() - 1] == '\''))
      value = value.substring(1, value.length() - 1);
    return value;
  }


  /**
   * Get the json representation of the matched element
   * <p>
   * example:
   * map [ {IP: 127.0.0.1}, {status:200}]
   * will return
   * {"IP":"127.0.0.1", "status":200}
   * </p>
   *
   * @return Json of the matched element in the text
   * @see google gson
   */
  public String toJson() {
    if (_capture == null)
      return "{\"Error\":\"Error\"}";
    if (_capture.isEmpty())
      return "{\"Error\":\"Error\"}";;

    this.cleanMap();
    Gson gs = new GsonBuilder().setPrettyPrinting().create();// new Gson();
    return gs.toJson(/* cleanMap( */_capture/* ) */);

  }

  /**
   * Get the map representation of the matched element in the text
   *
   * @see Map.toString();
   * @return map object from the matched element in the text
   */
  public Map<String, Object> toMap() {
    this.cleanMap();
    return _capture;
  }

  /**
   * Remove and rename the unwanted elelents in the matched map
   */
  private void cleanMap() {
    garbage.rename(_capture);
    garbage.remove(_capture);
  }

  /**
   * Util fct
   *
   * @return
   */
  public Boolean isNull() {
    if (this.match == null)
      return true;
    return false;
  }

  /**
   * Util fct
   *
   * @param s
   * @return
   */
  private boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }
}
