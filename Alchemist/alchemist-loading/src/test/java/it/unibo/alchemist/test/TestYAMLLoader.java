package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.collect.Maps;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.interfaces.Environment;

/**
 * A series of tests checking that our Yaml Loader is working as expected.
 */
public class TestYAMLLoader {

    /*
     * To run a single test, just change from "any two digits" to the exact test
     * number. Yay!
     */
    private static final String ISAC_REGEX = "\\d{2}-.*\\.yml";

    /**
     * Basic loading capabilities.
     */
    @Test
    public void testBase() {
        testNoVar("/testbase.yml");
    }

    /**
     * Test the ability to load a Protelis module from classpath.
     */
    @Test
    public void testLoadProtelisModule() {
        testNoVar("/test00.yml");
    }

    /**
     * Test the ability to inject variables.
     */
    @Test
    public void testLoadWIthVariable() {
        final Map<String, Double> map = Maps.newLinkedHashMap();
        map.put("testVar", 10d);
        testLoading("/test00.yml", map);
    }

    /**
     * Tests the whole laboratory lesson of the UniBo course
     * "Engineering of Adaptive Software Systems".
     * 
     * Useful as regression test.
     */
    @Test
    public void testISAC2016Lab() {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("isac"))
                .setScanners(new ResourcesScanner()));
        reflections.getResources(Pattern.compile(ISAC_REGEX))
            .parallelStream()
            .map(r -> "/" + r)
            .forEach(TestYAMLLoader::testNoVar);
    }

    private static void testNoVar(final String resource) {
        testLoading(resource, Collections.emptyMap());
    }

    private static <T> void testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = TestYAMLLoader.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T> env = new YamlLoader(res).getWith(vars);
        final Simulation<T> sim = new Engine<>(env, 10000);
        sim.addCommand(new Engine.StateCommand<T>().run().build());
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
    }

}
