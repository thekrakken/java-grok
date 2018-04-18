package io.krakens.grok.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * {@code Discovery} try to find the best pattern for the given string.
 *
 * @since 0.0.2
 */
public class Discovery {

  private Grok grok;

  /**
   * Create a new {@code Discovery} object.
   *
   * @param grok instance of grok
   */
  public Discovery(Grok grok) {
    this.grok = grok;
  }

  /**
   * Sort by regex complexity.
   *
   * @param groks Map of the pattern name and grok instance
   * @return the map sorted by grok pattern complexity
   */
  private Map<String, Grok> sort(Map<String, Grok> groks) {

    List<Grok> groky = new ArrayList<Grok>(groks.values());
    Map<String, Grok> grokMap = new LinkedHashMap<String, Grok>();
    Collections.sort(groky, new Comparator<Grok>() {
      public int compare(Grok g1, Grok g2) {
        return (this.complexity(g1.getNamedRegex()) < this.complexity(g2.getNamedRegex())) ? 1 : 0;
      }

      private int complexity(String expandedPattern) {
        int score = 0;
        score += expandedPattern.split("\\Q" + "|" + "\\E", -1).length - 1;
        score += expandedPattern.length();
        return score;
      }
    });

    for (Grok grok : groky) {
      grokMap.put(grok.getSaved_pattern(), grok);
    }
    return grokMap;

  }

  /**
   * Determinate the complexity of the pattern.
   *
   * @param expandedPattern regex string
   * @return the complexity of the regex
   */
  private int complexity(String expandedPattern) {
    int score = 0;

    score += expandedPattern.split("\\Q" + "|" + "\\E", -1).length - 1;
    score += expandedPattern.length();

    return score;
  }

  /**
   * Find a pattern from a log.
   *
   * @param text witch is the representation of your single
   * @return Grok pattern %{Foo}...
   */
  public String discover(String text) {
    if (text == null) {
      return "";
    }

    Map<String, Grok> groks = new TreeMap<String, Grok>();
    Map<String, String> grokPatterns = grok.getPatterns();
    // Boolean done = false;
    String texte = text;
    GrokCompiler compiler = GrokCompiler.newInstance();
    compiler.register(grokPatterns);

    // Compile the pattern
    for (Entry<String, String> stringStringEntry : grokPatterns.entrySet()) {
      @SuppressWarnings("rawtypes")
      Entry pairs = (Entry) stringStringEntry;
      String key = pairs.getKey().toString();

      try {
        Grok grok = compiler.compile("%{" + key + "}");
        grok.setSaved_pattern(key);
        groks.put(key, grok);
      } catch (Exception e) {
        // Add logger
      }

    }

    // Sort patterns by complexity
    Map<String, Grok> patterns = this.sort(groks);

    // while (!done){
    // done = true;
    for (Entry<String, Grok> pairs : patterns.entrySet()) {
      String key = pairs.getKey();
      Grok value = pairs.getValue();

      // We want to search with more complex pattern
      // We avoid word, small number, space....
      if (this.complexity(value.getNamedRegex()) < 20) {
        continue;
      }

      Match match = value.match(text);
      if (match.isNull()) {
        continue;
      }
      // get the part of the matched text
      String part = getPart(match, text);

      // we skip boundary word
      Pattern pattern = Pattern.compile(".\\b.");
      Matcher ma = pattern.matcher(part);
      if (!ma.find()) {
        continue;
      }

      // We skip the part that already include %{Foo}
      Pattern pattern2 = Pattern.compile("%\\{[^}+]\\}");
      Matcher ma2 = pattern2.matcher(part);

      if (ma2.find()) {
        continue;
      }
      texte = StringUtils.replace(texte, part, "%{" + key + "}");
    }
    // }

    return texte;
  }

  /**
   * Get the substring that match with the text.
   *
   * @param matcher Grok Match
   * @param text text
   * @return string
   */
  private String getPart(Match matcher, String text) {

    if (matcher == null || text == null) {
      return "";
    }

    return text.substring(matcher.getStart(), matcher.getEnd());
  }
}
