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


import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static java.lang.String.format;

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

  private final CharSequence subject; // texte
  private final Grok grok;
  private final Matcher match;
  private final int start;
  private final int end;

  private Map<String, Object> capture = Collections.emptyMap();

  /**
   * Create a new {@code Match} object.
   */
  public Match(CharSequence subject, Grok grok, Matcher match, int start, int end) {
    this.subject = subject;
    this.grok = grok;
    this.match = match;
    this.start = start;
    this.end = end;
  }

  /**
   * Create Empty grok matcher.
   */
  public static final Match EMPTY = new Match("", null, null, 0, 0);

  public Matcher getMatch() {
    return match;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  /**
   * Retrurn the single line of log.
   *
   * @return the single line of log
   */
  public CharSequence getSubject() {
    return subject;
  }

  /**
   * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map.
   *
   * Multiple values for the same key are stored as list.
   *
   */
  public Map<String, Object> capture() {
    return capture(false);
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
   * See also {@link #capture} which returns multiple values of the same key as list.
   *
   */
  public Map<String, Object> captureFlattened() {
    return capture(true);
  }

  private Map<String, Object> capture(boolean flattened ) {
    if (match == null) {
      return Collections.emptyMap();
    }

    if (!capture.isEmpty()) {
      return capture;
    }

    capture = Maps.newHashMap();

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
      if (valueString != null) {
        IConverter converter = grok.converters.get(key);

        if (converter != null) {
          key = Converter.extractKey(key);
          try {
            value = converter.convert(valueString);
          } catch (Exception e) {
            capture.put(key + "_grokfailure", e.toString());
          }

          if (value instanceof String) {
            value = cleanString((String) value);
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

    capture = Collections.unmodifiableMap(capture);

    return capture;
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
  public String toJson(boolean pretty) {
    if (capture == null) {
      return "{}";
    }
    if (capture.isEmpty()) {
      return "{}";
    }

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
   * Util fct.
   *
   * @return boolean
   */
  public Boolean isNull() {
    return this.match == null;
  }

}
