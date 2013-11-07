package com.nflabs.Grok;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Grok {
    // manage string like %{Foo} => Foo
    private static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern.compile("%\\{(.*?)\\}");
    private static final java.util.regex.Pattern GROK_FILE_ENTRY_PATTERN = java.util.regex.Pattern.compile("^([A-z0-9_]+)\\s+(.*)$");
    private static final Pattern PATTERN_RE = Pattern.compile("%\\{" +
            "(?<name>" +
            "(?<pattern>[A-z0-9]+)" +
            "(?::(?<subname>[A-z0-9_:]+))?" +
            ")" +
            "(?:=(?<definition>" +
            "(?:" +
            "(?:[^{}]+|\\.+)+" +
            ")+" +
            ")" +
            ")?" +
            "\\}");

    private Map<String, String> patterns;
    private Map<String, String> capturedMap;
    private String savedPattern;
    private String expandedPattern;
    private Pattern regexp;
    private Discovery disco;

    /**
     * * Constructor.
     */
    public Grok() {
        patterns = new HashMap<String, String>();
        capturedMap = new HashMap<String, String>();
    }

    /**
     * Add a new pattern
     *
     * @param name    Name of the pattern
     * @param pattern regex string
     */
    public void addPattern(String name, String pattern) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is null or empty");
        }
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("pattern is null or empty");
        }
        patterns.put(name, pattern);
    }

    /**
     * Copy the map patterns into the grok pattern
     *
     * @param cpy of the pattern to copy
     */
    public void copyPatterns(Map<String, String> cpy) {
        patterns.putAll(cpy);
    }

    /**
     * @return the current map grok patterns
     */
    public Map<String, String> getPatterns() {
        return this.patterns;
    }

    /**
     * @return the compiled regex of <tt>expanded_pattern</tt>
     * @see #compile(String)
     */
    public Pattern getRegEx() {
        return regexp;
    }

    /**
     * @return the string pattern
     * @see #compile(String)
     */
    public String getExpandedPattern() {
        return expandedPattern;
    }

    /**
     * Add patterns to grok from a file
     *
     * @param file that contains the grok patterns
     * @throws IOException
     */
    public void addPatternFromFile(String file) throws IOException {
        addPatternFromReader(new FileReader(new File(file)));
    }

    /**
     * Add patterns to grok from a reader
     *
     * @param r that contains the grok patterns
     */
    public void addPatternFromReader(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String line;
        //We dont want \n and commented line
        while ((line = br.readLine()) != null) {
            java.util.regex.Matcher m = GROK_FILE_ENTRY_PATTERN.matcher(line);
            if (m.matches()) {
                addPattern(m.group(1), m.group(2));
            }
        }
        br.close();
    }

    /**
     * Match the <tt>text</tt> with the pattern
     *
     * @param text to match
     * @return Grok Match
     * @see Match
     */
    public Match match(String text) {
        if (regexp == null) {
            throw new IllegalStateException("Pattern is not initialized call compile(String)");
        }
        Matcher m = regexp.matcher(text);
        if (!m.find()) {
            return null;
        }
        Match match = new Match(this);
        match.setSubject(text);
        match.setMatch(m);
        return match;
    }

    /**
     * Transform grok regex into a compiled regex
     *
     * @param pattern pattern regex
     */
    public void compile(String pattern) {
        expandedPattern = pattern;
        int index = 0;
        boolean Continue = true;

        //Replace %{foo} with the regex (mostly groupname regex)
        //and then compile the regex
        while (Continue) {
            Continue = false;

            Matcher m = PATTERN_RE.matcher(expandedPattern);
            // Match %{Foo:bar} -> pattern name and subname
            // Match %{Foo=regex} -> add new regex definition
            if (m.find()) {
                Continue = true;
                Map<String, String> group = m.namedGroups();

                if (group.get("definition") != null) {
                    addPattern(group.get("pattern"), group.get("definition"));
                    group.put("name", group.get("name") + "=" + group.get("definition"));
                    //System.out.println("%{"+group.get("name")+"} =>" + this.patterns.get(group.get("pattern")));
                }
                capturedMap.put("name" + index, (group.get("subname") != null ? group.get("subname") : group.get("name")));
                expandedPattern = StringUtils.replace(expandedPattern, "%{" + group.get("name") + "}", "(?<name" + index + ">" + this.patterns.get(group.get("pattern")) + ")");
//                System.out.println(expandedPattern);
                index++;
            }
        }
        //System.out.println(capturedMap);
        //Compile the regex
        if (!expandedPattern.isEmpty()) {
            regexp = Pattern.compile(expandedPattern);
            return;
        }
        throw new IllegalStateException("Pattern not found: " + pattern);
    }

    /**
     * Grok can find the pattern
     *
     * @param input the file to analyze
     * @return the grok pattern
     */
    public String discover(String input) {

        if (disco == null) {
            disco = new Discovery(this);
        }
        return disco.discover(input);
    }

    /**
     * @param id the id
     * @return the value
     */
    public String captureName(String id) {
        return capturedMap.get(id);
    }

    /**
     * @return getter
     */
    public Map<String, String> getCaptured() {
        return capturedMap;
    }

    /**
     * * Checkers
     */
    public boolean isPattern() {
        return !(patterns == null || patterns.isEmpty());
    }

    public String getSavedPattern() {
        return savedPattern;
    }

    public void setSavedPattern(String savedPattern) {
        this.savedPattern = savedPattern;
    }
}
