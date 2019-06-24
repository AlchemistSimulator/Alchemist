/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.ClassPathScanner;
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
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A series of tests checking that our Yaml Loader is working as expected.
 */
public class TestYAMLLoader {

    /*
     * To run a single test, just change from "any two digits" to the exact test
     * number. Yay!
     */
    private static final String ISAC_REGEX = ".*/\\d{2}-.*\\.yml";

    /**
     * Tests building a custom implementation of time distribution.
     */
    @Test
    public void testAnyRealDistribution() {
        final Environment<?, ?> env = testNoVar("synthetic/anyrealdistribution.yml");
        env.forEach(n -> n.forEach(r -> {
            assertTrue(r.getTimeDistribution() instanceof AnyRealDistribution);
        }));
    }

    /**
     * Test loading a custom node class.
     */
    @Test
    public void testCustomNodes() {
        testNoVar("synthetic/customnode.yml")
        .forEach(n -> assertTrue(n instanceof TestNode,
                "Node are not instances of " + TestNode.class.getName()
                + " as expected, but " + n.getClass().getName() + " instead"));
    }

    /**
     * Tests the whole laboratory lesson of the UniBo course
     * "Engineering of Adaptive Software Systems".
     * 
     * Useful as regression test.
     */
    @Test
    public void testISAC2016Lab() {
        ClassPathScanner.resourcesMatchingAsStream(ISAC_REGEX, "isac")
            .forEach(TestYAMLLoader::testNoVar);
    }

    /**
     * Test loading layer classes.
     *
     * @param <P> Used for internal consistency
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
        final Incarnation<?, ?> inc = SupportedIncarnations.get("sapere").orElseThrow(
                () -> new IllegalStateException("No SAPERE incarnation available"));
        final Molecule a = inc.createMolecule("A");
        assertTrue(env.getLayer(a).isPresent());
        assertTrue(env.getLayer(a).get() instanceof StepLayer);
        final Molecule b = inc.createMolecule("B");
        assertTrue(env.getLayer(b).isPresent());
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
        env.forEach(n -> assertEquals(4, n.getChemicalSpecies()));
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

    private static <T, P extends Position<P>> Environment<T, P> testLoading(final InputStream resource, final Map<String, Double> vars) {
        assertNotNull(resource, "Missing test resource " + resource);
        final Environment<T, P> env = new YamlLoader(resource).getWith(vars);
        final Simulation<T, P> sim = new Engine<>(env, 10000);
        sim.play();
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
        sim.getError().ifPresent(e -> fail(e.getMessage()));
        return env;
    }

    private static <T> Environment<T, ?> testNoVar(final InputStream resource) {
        return testLoading(resource, Collections.emptyMap());
    }

    private static <T> Environment<T, ?> testNoVar(final String resource) {
        return testLoading(ResourceLoader.getResourceAsStream(resource), Collections.emptyMap());
    }

}
