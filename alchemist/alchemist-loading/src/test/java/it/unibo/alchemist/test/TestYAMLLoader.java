/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.test;

import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.layers.StepLayer;
import it.unibo.alchemist.model.implementations.timedistributions.AnyRealDistribution;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.test.util.TestNode;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        final Environment<?, ?> env = testNoVar("synthetic/anyrealdistribution.yml");
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
        testNoVar("synthetic/customnode.yml")
        .forEach(n -> assertTrue(
                "Node are not instances of " + TestNode.class.getName()
                + " as expected, but " + n.getClass().getName() + " instead",
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
            .forEach(TestYAMLLoader::testNoVar);
    }

    /**
     * Test loading layer classes.
     */
    @Test
    public <P extends Position<P>> void testLayers() {
        @SuppressWarnings("unchecked")
        final Environment<Object, P> env = (Environment<Object, P>) testNoVar("synthetic/testlayer.yml");
        final Set<Layer<Object, P>> layers = env.getLayers();
        assertFalse(layers.isEmpty());
        assertEquals(2, layers.size());
        assertEquals(2L, layers.stream()
                .filter(l -> l instanceof StepLayer)
                .count());
        final Incarnation<?, ?> inc = SupportedIncarnations.get("sapere").get();
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
        assertNotNull(testNoVar("synthetic/testlist.yml"));
    }

    /**
     * Tests injecting multiple molecules in the same shape.
     */
    @Test
    public void testMultipleMolecules() {
        final Environment<?, ?> env = testNoVar("synthetic/multiplemolecule.yml");
        env.forEach(n -> {
            assertEquals(4, n.getChemicalSpecies());
        });
    }

    /**
     * Test loading layer classes.
     */
    @Test
    public void testSingleValuedGeometricVar() {
        assertNotNull(testNoVar("synthetic/singleValuedGeometricVar.yml"));
    }

    /**
     * Test variables with same structure but different names.
     */
    @Test
    public void testVariableContentClash() {
        assertNotNull(testNoVar("synthetic/varcontentclash.yml"));
    }

    /**
     * Test variables with same structure but different names.
     */
    @Test
    public void testScalaVar() {
        final Environment<Object, ?> env = testNoVar("synthetic/scalavar.yml");
        assertNotNull(env);
        assertEquals(env.makePosition(3, 10), env.getPosition(env.getNodeByID(0)));
    }

    /**
     * Test dependencies section.
     */
    @Test
    public void testDependencies() {
        final InputStream is = ResourceLoader.getResourceAsStream("isac/16-dependencies.yaml");
        assertNotNull(is);
        final Loader loader = new YamlLoader(is);
        final List<String> dependencies = loader.getDependencies();
        assertEquals(dependencies.size(), 2);
        assertEquals(dependencies.get(0), "dependencies_test.txt");
    }

    private static <T, P extends Position<P>> Environment<T, P> testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = ResourceLoader.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T, P> env = new YamlLoader(res).getWith(vars);
        final Simulation<T, P> sim = new Engine<>(env, 10000);
        sim.play();
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
        sim.getError().ifPresent(e -> Assert.fail(e.getMessage()));
        return env;
    }

    private static <T> Environment<T, ?> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }

}
