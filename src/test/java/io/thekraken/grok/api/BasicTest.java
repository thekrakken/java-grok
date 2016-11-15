package io.thekraken.grok.api;

import io.thekraken.grok.api.exception.GrokException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicTest {

    @Test
    public void test001_compileFailOnInvalidExpression() throws GrokException {

        Grok g = Grok.create(ResourceManager.PATTERNS, null);

        List<String> badRegxp = new ArrayList<String>();
        badRegxp.add("[");
        badRegxp.add("[foo");
        badRegxp.add("?");
        badRegxp.add("foo????");
        badRegxp.add("(?-");

        boolean thrown = false;

        /** This should always throw */
        for (String regx : badRegxp) {
            try {
                g.compile(regx);
            } catch (PatternSyntaxException e) {
                thrown = true;
            }
            assertTrue(thrown);
            thrown = false;
        }
    }

    @Test
    public void test002_compileSuccessValidExpression() throws GrokException {

        Grok g = Grok.create(ResourceManager.PATTERNS);

        List<String> regxp = new ArrayList<String>();
        regxp.add("[hello]");
        regxp.add("(test)");
        regxp.add("(?:hello)");
        regxp.add("(?=testing)");

        for (String regx : regxp) {
            g.compile(regx);
        }
    }

    @Test
    public void test003_samePattern() throws GrokException {
        Grok g = Grok.create(ResourceManager.PATTERNS);

        String pattern = "Hello World";
        g.compile(pattern);
        assertEquals(pattern, g.getOriginalGrokPattern());
    }

    @Test
    public void test004_sameExpantedPatern() throws GrokException {
        Grok g = Grok.create(ResourceManager.PATTERNS);

        g.addPattern("test", "hello world");
        g.compile("%{test}");
        assertEquals("(?<name0>hello world)", g.getNamedRegex());
    }

    @Test
    public void test005_testLoadPatternFromFile() throws IOException, GrokException {
        File temp = File.createTempFile("grok-tmp-pattern", ".tmp");
        getClass();
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write("TEST \\d+");
        bw.close();

        Grok grok = Grok.create(temp.getAbsolutePath());
        grok.compile("%{TEST}");
        assertEquals("(?<name0>\\d+)", grok.getNamedRegex());
        temp.delete();
    }

}
