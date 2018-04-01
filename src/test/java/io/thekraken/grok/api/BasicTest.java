package io.thekraken.grok.api;

import com.google.common.io.Resources;
import io.thekraken.grok.api.exception.GrokException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicTest {
    private GrokCompiler compiler;

    @Before
    public void setup() throws Exception {
        compiler = GrokCompiler.newInstance();
        compiler.register(Resources.getResource(ResourceManager.PATTERNS).openStream());
    }

    @Test
    public void test001_compileFailOnInvalidExpression() throws GrokException {
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
                compiler.compile(regx);
            } catch (PatternSyntaxException e) {
                thrown = true;
            }
            assertTrue(thrown);
            thrown = false;
        }
    }

    @Test
    public void test002_compileSuccessValidExpression() throws GrokException {
        List<String> regxp = new ArrayList<String>();
        regxp.add("[hello]");
        regxp.add("(test)");
        regxp.add("(?:hello)");
        regxp.add("(?=testing)");

        for (String regx : regxp) {
            compiler.compile(regx);
        }
    }

    @Test
    public void test003_samePattern() throws GrokException {
        String pattern = "Hello World";
        Grok g = compiler.compile(pattern);
        assertEquals(pattern, g.getOriginalGrokPattern());
    }

    @Test
    public void test004_sameExpantedPatern() throws GrokException {
        compiler.register("test", "hello world");
        Grok g = compiler.compile("%{test}");
        assertEquals("(?<name0>hello world)", g.getNamedRegex());
    }

    @Test
    public void test005_testLoadPatternFromFile() throws IOException, GrokException {
        File temp = File.createTempFile("grok-tmp-pattern", ".tmp");
        getClass();
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write("TEST \\d+");
        bw.close();

        GrokCompiler compiler = GrokCompiler.newInstance();
        compiler.register(new FileInputStream(temp));
        Grok grok = compiler.compile("%{TEST}");
        assertEquals("(?<name0>\\d+)", grok.getNamedRegex());
        temp.delete();
    }

}
