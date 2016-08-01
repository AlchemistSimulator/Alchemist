package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.layers.EmptyLayer;
import it.unibo.alchemist.model.implementations.layers.StepLayer;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.test.util.TestNode;

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

    /**
     * Test loading a custom node class.
     */
    @Test
    public void testCustomNodes() {
        testNoVar("/synthetic/customnode.yml")
        .forEach(n -> assertTrue(
                "Node are not instances of " + TestNode.class.getName() + " as expected, but " + n.getClass().getName() + " instead",
                n instanceof TestNode));
    }

    /**
     * Test loading layer classes.
     */
    @Test
    public void testLayers() {
        final Set<Layer<Object>> layers = testNoVar("/synthetic/testlayer.yml").getLayers();
        assertFalse(layers.isEmpty());
        assertEquals(3L, layers.stream()
                .count());
        assertEquals(2L, layers.stream()
                .filter(l -> l instanceof StepLayer)
                .count());
        assertEquals(1L, layers.stream()
                .filter(l -> l instanceof EmptyLayer)
                .count());
    }

    private static <T> Environment<T> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }

    private static <T> Environment<T> testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = TestYAMLLoader.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T> env = new YamlLoader(res).getWith(vars);
        final Simulation<T> sim = new Engine<>(env, 10000);
        sim.addCommand(new Engine.StateCommand<T>().run().build());
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
        return env;
    }

}
