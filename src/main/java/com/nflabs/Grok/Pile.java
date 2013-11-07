package com.nflabs.Grok;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class Pile {

    //Private
    private List<Grok> groks;
    private Map<String, String> patterns;
    private List<String> patternFiles;

    static final String defaultPatternDirectory = "patterns/";

    /**
     * * Constructor
     */
    public Pile() {
        patterns = new TreeMap<String, String>();
        groks = new ArrayList<Grok>();
        patternFiles = new ArrayList<String>();
    }

    /**
     * @param name of the pattern
     * @param file path
     */
    public void addPattern(String name, String file) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is null or empty");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is null or empty");
        }
        patterns.put(name, file);
    }

    /**
     * Load patterns file from a directory
     *
     * @param directory the directory
     */
    public void addFromDirectory(String directory) throws FileNotFoundException {

        if (directory == null || directory.isEmpty()) {
            directory = defaultPatternDirectory;
        }

        File dir = new File(directory);
        if (!dir.exists()) {
            throw new FileNotFoundException(dir.getAbsolutePath());
        }
        File lst[] = dir.listFiles();

        if (lst == null) {
            return;
        }
        for (File aLst : lst) {
            if (aLst.isFile()) {
                addPatternFromFile(aLst.getAbsolutePath());
            }
        }
    }


    /**
     * Add pattern to grok from a file
     *
     * @param file the file
     */
    public void addPatternFromFile(String file) throws FileNotFoundException {
        File f = new File(file);
        if (!f.exists()) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        patternFiles.add(file);
    }

    /**
     * Compile the pattern with a corresponding grok
     *
     * @param pattern the pattern
     * @throws IOException
     */
    public void compile(String pattern) throws IOException {

        Grok grok = new Grok();

        Set<String> added = new HashSet<String>();

        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            if (!added.add(entry.getValue())) {
                grok.addPattern(entry.getKey(), entry.getValue());
            }
        }

        for (String file : patternFiles) {
            grok.addPatternFromFile(file);
        }

        grok.compile(pattern);
        groks.add(grok);
    }

    /**
     * @param line to match
     * @return Grok Match
     */
    public Match match(String line) {
        for (Grok grok : groks) {
            Match gm = grok.match(line);
            if (gm != null) {
                return gm;
            }
        }

        return null;
    }

}
