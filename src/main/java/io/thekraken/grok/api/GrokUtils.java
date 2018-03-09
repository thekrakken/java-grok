package io.thekraken.grok.api;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * {@code GrokUtils} contain set of useful tools or methods.
 *
 * @author anthonycorbacho
 * @since 0.0.6
 */
public class GrokUtils {

  /**
   * Extract Grok patter like %{FOO} to FOO, Also Grok pattern with semantic.
   */
  public static final Pattern GROK_PATTERN = Pattern.compile(
      "%\\{" +
      "(?<name>" +
        "(?<pattern>[A-z0-9]+)" +
          "(?::(?<subname>[A-z0-9_:;\\/\\s\\.]+))?" +
          ")" +
          "(?:=(?<definition>" +
            "(?:" +
            "(?:[^{}]+|\\.+)+" +
            ")+" +
            ")" +
      ")?" +
      "\\}");
  
  public static final Pattern NAMED_REGEX = Pattern
      .compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

  public static Set<String> getNameGroups(String regex) {
    Set<String> namedGroups = new LinkedHashSet<>();
    Matcher m = NAMED_REGEX.matcher(regex);
    while (m.find()) {
      namedGroups.add(m.group(1));
    }
    return namedGroups;
  }

  public static Map<String, String> namedGroups(Matcher matcher, Set<String> groupNames) {
    Map<String, String> namedGroups = new LinkedHashMap<String, String>();
    for (String groupName : groupNames) {
      String groupValue = matcher.group(groupName);
      namedGroups.put(groupName, groupValue);
    }
    return namedGroups;
  }
}
