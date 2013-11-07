package com.nflabs.Grok;

import java.util.*;


@SuppressWarnings("UnusedDeclaration")
public class CleanUpHandler {

    private Set<String> remove;
    private Set<String> retain;
    private Map<String, String> rename;

    /**
     * Set a map of matched field to re name
     *
     * @param key   name
     * @param value name
     */
    public void addToRename(String key, String value) {
        if (rename == null) {
            rename = new HashMap<String, String>();
        }
        rename.put(key, value);
    }

    /**
     * Set a field list to remove from the final matched map
     *
     * @param item to remove
     */
    public void addToRemove(String item) {
        if (remove == null) {
            remove = new HashSet<String>();
        }
        remove.add(item);
    }

    /**
     * @param fields the list of strings, to remove from the final match
     */
    public void addToRemove(Collection<String> fields) {
        if (remove == null) {
            remove = new HashSet<String>();
        }
        remove.addAll(fields);
    }

    /**
     * @param fields the list of strings, to remove from the final match
     */
    public void addToRemove(String... fields) {
        if (remove == null) {
            remove = new HashSet<String>();
        }
        Collections.addAll(remove, fields);
    }

    /**
     * Set a field to retain in the final match
     *
     * @param item to remove
     */
    public void addToRetain(String item) {
        if (retain == null) {
            retain = new HashSet<String>();
        }
        retain.add(item);
    }

    /**
     * @param fields the list of strings, to retain in the final match
     */
    public void addToRetain(Collection<String> fields) {
        if (retain == null) {
            retain = new HashSet<String>();
        }
        retain.addAll(fields);
    }

    /**
     * @param fields the list of strings, to retain in the final match
     */
    public void addToRetain(String... fields) {
        if (retain == null) {
            retain = new HashSet<String>();
        }
        Collections.addAll(retain, fields);
    }

    public <T> void handle(Map<String, T> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        if (remove != null) {
            data.keySet().removeAll(remove);
        }
        if (retain != null) {
            data.keySet().retainAll(retain);
        }
        if (rename != null) {
            for (Map.Entry<String, String> r : rename.entrySet()) {
                if (data.containsKey(r.getKey())) {
                    data.put(r.getValue(), data.remove(r.getKey()));
                }
            }
        }
    }

}
