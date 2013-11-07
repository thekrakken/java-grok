package com.nflabs.Grok;


import com.google.code.regexp.Matcher;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

@SuppressWarnings("UnusedDeclaration")
public class Match {

    private Grok grok;    //current grok instance
    private Matcher match;    //regex matcher
    private CleanUpHandler cleanUpHandler;
    private String subject;
    private Map<String, String> capture;

    public Match(Grok grok) {
        this.grok = grok;
    }

    public void setCleanUpHandler(CleanUpHandler cleanUpHandler) {
        this.cleanUpHandler = cleanUpHandler;
    }

    /**
     * @param text to analyze / save
     */
    public void setSubject(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("text is null or empty");
        }
        subject = text;
    }

    /**
     * Getter
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map
     *
     * @see #getSubject()
     * @see #toJson()
     */
    public void captures() {
        if (match == null) {
            throw new IllegalStateException("math is null");
        }
        capture = new TreeMap<String, String>();
        for (Entry<String, String> pairs : match.namedGroups().entrySet()) {
            String key = grok.captureName(pairs.getKey());
            if (key == null || key.isEmpty()) {
                continue;
            }
            capture.put(key, pairs.getValue());
        }
    }

    /**
     * remove from the string the quote and dquote
     *
     * @param value to pure: "my/text"
     * @return unquoted string: my/text
     */
    private String cleanString(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char[] tmp = value.toCharArray();
        if ((tmp[0] == '"' && tmp[value.length() - 1] == '"')
                || (tmp[0] == '\'' && tmp[value.length() - 1] == '\'')) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }


    /**
     * @return Json file from the matched element in the text
     */
    public String toJson() {
        if (capture == null) {
            return "{\"Error\":\"Error\"}";
        }
        if (capture.isEmpty()) {
            return null;
        }
        return new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(toMap());
    }

    /**
     * @return java map object from the matched element in the text
     */
    public Map<String, String> toMap() {
        return cleanMap();
    }

    /**
     * remove and/or rename items
     */
    private Map<String, String> cleanMap() {
        if (capture == null) {
            throw new IllegalStateException("you need to call captures() first");
        }
        if (cleanUpHandler != null) {
            cleanUpHandler.handle(capture);
        }
        return capture;
    }

    /**
     */
    public Boolean isNull() {
        return match == null;
    }

    public Matcher getMatch() {
        return match;
    }

    public void setMatch(Matcher match) {
        this.match = match;
    }

    public int getStart() {
        return match.start();
    }

    public int getEnd() {
        return match.end();
    }

}
