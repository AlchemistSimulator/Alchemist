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

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.layers.StepLayer;
import it.unibo.alchemist.model.implementations.timedistributions.AnyRealDistribution;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Molecule;
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
     * Tests building a custom implementation of time distribution.
     */
    @Test
    public void testAnyRealDistribution() {
        final Environment<?> env = testNoVar("/synthetic/anyrealdistribution.yml");
        env.forEach(n -> {
            n.forEach(r -> {
                assertTrue(r.getTimeDistribution() instanceof AnyRealDistribution);
            });
        });
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
            .stream()
            .map(r -> "/" + r)
            .forEach(TestYAMLLoader::testNoVar);
    }

    /**
     * Test loading layer classes.
     */
    @Test
    public void testLayers() {
        final Environment<Object> env = testNoVar("/synthetic/testlayer.yml");
        final Set<Layer<Object>> layers = env.getLayers();
        assertFalse(layers.isEmpty());
        assertEquals(2, layers.size());
        assertEquals(2L, layers.stream()
                .filter(l -> l instanceof StepLayer)
                .count());
        final Incarnation<?> inc = SupportedIncarnations.get("sapere").get();
        final Molecule a = inc.createMolecule("A");
        assertTrue(env.getLayer(a).get() instanceof StepLayer);
        final Molecule b = inc.createMolecule("B");
        assertTrue(env.getLayer(b).get() instanceof StepLayer);
    }

    /**
     * Test loading layer classes.
     */
    @Test
    public void testLoadVariablesInLists() {
        assertNotNull(testNoVar("/synthetic/testlist.yml"));
    }

    /**
     * Tests injecting multiple molecules in the same shape.
     */
    @Test
    public void testMultipleMolecules() {
        final Environment<?> env = testNoVar("/synthetic/multiplemolecule.yml");
        env.forEach(n -> {
            assertEquals(4, n.getChemicalSpecies());
        });
    }

    /**
     * Test loading layer classes.
     */
    @Test
    public void testSingleValuedGeometricVar() {
        assertNotNull(testNoVar("/synthetic/singleValuedGeometricVar.yml"));
    }

    /**
     * Test variables with same structure but different names.
     */
    @Test
    public void testVariableContentClash() {
        assertNotNull(testNoVar("/synthetic/varcontentclash.yml"));
    }

    /**
     * Test variables with same structure but different names.
     */
    @Test
    public void testScalaVar() {
        final Environment<Object> env = testNoVar("/synthetic/scalavar.yml");
        assertNotNull(env);
        assertEquals(env.makePosition(3, 10), env.getPosition(env.getNodeByID(0)));
    }

    private static <T> Environment<T> testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = TestYAMLLoader.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T> env = new YamlLoader(res).getWith(vars);
        final Simulation<T> sim = new Engine<>(env, 10000);
        sim.play();
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
        sim.getError().ifPresent(e -> Assert.fail(e.getMessage()));
        return env;
    }

    private static <T> Environment<T> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }

}
