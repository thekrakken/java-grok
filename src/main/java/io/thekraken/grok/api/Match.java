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


import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.thekraken.grok.api.exception.GrokException;

/**
 * {@code Match} is a representation in {@code Grok} world of your log.
 *
 * @author anthonycorbacho
 * @since 0.0.1
 */
public class Match {

  private static final Gson PRETTY_GSON =
          new GsonBuilder().setPrettyPrinting().create();
  private static final Gson GSON = new GsonBuilder().create();

  private String subject; // texte
  private Map<String, Object> capture;
  private Garbage garbage;
  private Grok grok;
  private Matcher match;
  private int start;
  private int end;

  /**
   * For thread safety.
   */
  private static ThreadLocal<Match> matchHolder = new ThreadLocal<Match>() {
    @Override
    protected Match initialValue() {
      return new Match();
    }
  };

  /**
   * Create a new {@code Match} object.
   */
  public Match() {
    subject = "Nothing";
    grok = null;
    match = null;
    capture = new TreeMap<String, Object>();
    garbage = new Garbage();
    start = 0;
    end = 0;
  }

  /**
   * Create Empty grok matcher.
   */
  public static final Match EMPTY = new Match();

  public void setGrok(Grok grok) {
    if (grok != null) {
      this.grok = grok;
    }
  }

  public Matcher getMatch() {
    return match;
  }

  public void setMatch(Matcher match) {
    this.match = match;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  /**
   * Singleton.
   *
   * @return instance of Match
   */
  public static Match getInstance() {
    return matchHolder.get();
  }

  /**
   * Set the single line of log to parse.
   *
   * @param text : single line of log
   */
  public void setSubject(String text) {
    if (text == null) {
      return;
    }
    if (text.isEmpty()) {
      return;
    }
    subject = text;
  }

  /**
   * Retrurn the single line of log.
   *
   * @return the single line of log
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map.
   *
   * Multiple values for the same key are stored as list.
   *
   */
  public void captures() {
    captures(false);

  }

  /**
   * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map
   *
   * Multiple values to the same key are flattened to one value: the sole non-null value will be captured.
   * Should there be multiple non-null values a RuntimeException is being thrown.
   *
   * This can be used in cases like: (foo (.*:message) bar|bar (.*:message) foo) where the regexp guarantees that only
   * one value will be captured.
   *
   * See also {@link #captures} which returns multiple values of the same key as list.
   *
   */
  public void capturesFlattened() {
    captures(true);
  }

  @SuppressWarnings("unchecked")
  private void captures(boolean flattened ) {
    if (match == null) {
      return;
    }

    capture.clear();
    boolean automaticConversionEnabled = grok.isAutomaticConversionEnabled();

    // _capture.put("LINE", this.line);
    // _capture.put("LENGTH", this.line.length() +"");

    Map<String, String> mappedw = GrokUtils.namedGroups(this.match, this.grok.namedGroups);

    mappedw.forEach((key, valueString) -> {
      String id = this.grok.getNamedRegexCollectionById(key);
      if (id != null && !id.isEmpty()) {
        key = id;
      }

      if ("UNWANTED".equals(key)) {
        return;
      }

      Object value = valueString;
      if (valueString != null && automaticConversionEnabled) {
        if (Converter.DELIMITER.matchesAnyOf(key)) {
          KeyValue keyValue = Converter.convert(key, valueString);

          // get validated key
          key = keyValue.getKey();

          // resolve value
          if (keyValue.getValue() instanceof String) {
            value = cleanString((String) keyValue.getValue());
          } else {
            value = keyValue.getValue();
          }

          // set if grok failure
          if (keyValue.hasGrokFailure()) {
            capture.put(key + "_grokfailure", keyValue.getGrokFailure());
          }
        } else {
          value = cleanString(valueString);
        }
      }

      if (capture.containsKey(key)) {
        Object currentValue = capture.get(key);

        if (flattened) {
          if (currentValue == null && value != null) {
            capture.put(key, value);
          } if (currentValue != null && value != null) {
            throw new RuntimeException(
                format("key '%s' has multiple non-null values, this is not allowed in flattened mode, values:'%s', '%s'",
                    key,
                    currentValue,
                    value));
          }
        } else {
          if (currentValue instanceof List) {
            ((List<Object>) currentValue).add(value);
          } else {
            List<Object> list = new ArrayList<Object>();
            list.add(currentValue);
            list.add(value);
            capture.put(key, list);
          }
        }
      } else {
        capture.put(key, value);
      }
    });
  }

  /**
   * remove from the string the quote and double quote.
   *
   * @param value string to pure: "my/text"
   * @return unquoted string: my/text
   */
  private String cleanString(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }

    char firstChar = value.charAt(0);
    char lastChar = value.charAt(value.length() - 1);

    if (firstChar == lastChar && (firstChar == '"' || firstChar == '\'')) {
      if (value.length() == 1) {
        return "";
      } else {
        return value.substring(1, value.length() - 1);
      }
    }

    return value;
  }


  /**
   * Get the json representation of the matched element.
   * <p>
   * example: map [ {IP: 127.0.0.1}, {status:200}] will return {"IP":"127.0.0.1", "status":200}
   * </p>
   * If pretty is set to true, json will return prettyprint json string.
   *
   * @return Json of the matched element in the text
   */
  public String toJson(Boolean pretty) {
    if (capture == null) {
      return "{}";
    }
    if (capture.isEmpty()) {
      return "{}";
    }

    this.cleanMap();
    Gson gs;
    if (pretty) {
      gs = PRETTY_GSON;
    } else {
      gs = GSON;
    }
    return gs.toJson(/* cleanMap( */capture/* ) */);
  }

  /**
   * Get the json representation of the matched element.
   * <p>
   * example: map [ {IP: 127.0.0.1}, {status:200}] will return {"IP":"127.0.0.1", "status":200}
   * </p>
   *
   * @return Json of the matched element in the text
   */
  public String toJson() {
    return toJson(false);
  }

  /**
   * Get the map representation of the matched element in the text.
   *
   * @return map object from the matched element in the text
   */
  public Map<String, Object> toMap() {
    this.cleanMap();
    return capture;
  }

  /**
   * Remove and rename the unwanted elements in the matched map.
   */
  private void cleanMap() {
    garbage.rename(capture);
    garbage.remove(capture);
  }

  /**
   * Util fct.
   *
   * @return boolean
   */
  public Boolean isNull() {
    return this.match == null;
  }

}
