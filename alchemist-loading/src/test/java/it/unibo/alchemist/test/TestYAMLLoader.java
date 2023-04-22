/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.providers.YamlProvider;
import it.unibo.alchemist.model.SupportedIncarnations;
import it.unibo.alchemist.model.implementations.layers.StepLayer;
import it.unibo.alchemist.model.implementations.timedistributions.AnyRealDistribution;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Layer;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.test.util.TestNode;
import it.unibo.alchemist.util.ClassPathScanner;
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
class TestYAMLLoader {

    /*
     * To run a single test, just change from "any two digits" to the exact test
     * number. Yay!
     */
    private static final String ISAC_REGEX = ".*/\\d{2}-.*\\.yml";

    /**
     * Tests building a custom implementation of time distribution.
     */
    @Test
    void testAnyRealDistribution() {
        final Environment<?, ?> environment = testNoVar("synthetic/anyrealdistribution.yml");
        environment.forEach(n -> n.forEach(r -> assertTrue(r.getTimeDistribution() instanceof AnyRealDistribution)));
    }

    /**
     * Test loading a custom node class.
     */
    @Test
    void testCustomNodes() {
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
    void testISAC2016Lab() {
        ClassPathScanner.resourcesMatchingAsStream(ISAC_REGEX, "isac")
            .forEach(TestYAMLLoader::testNoVar);
    }

    /**
     * Test loading layer classes.
     *
     * @param <P> Used for internal consistency
     */
    @Test
    <P extends Position<P>> void testLayers() {
        @SuppressWarnings("unchecked")
        final Environment<Object, P> environment = (Environment<Object, P>) testNoVar("synthetic/testlayer.yml");
        final Set<Layer<Object, P>> layers = environment.getLayers();
        assertFalse(layers.isEmpty());
        assertEquals(2, layers.size());
        assertEquals(2L, layers.stream()
                .filter(l -> l instanceof StepLayer)
                .count());
        final Incarnation<?, ?> inc = SupportedIncarnations.get("sapere").orElseThrow(
                () -> new IllegalStateException("No SAPERE incarnation available"));
        final Molecule a = inc.createMolecule("A");
        assertTrue(environment.getLayer(a).isPresent());
        assertTrue(environment.getLayer(a).get() instanceof StepLayer);
        final Molecule b = inc.createMolecule("B");
        assertTrue(environment.getLayer(b).isPresent());
        assertTrue(environment.getLayer(b).get() instanceof StepLayer);
    }

    /**
     * Test loading layer classes.
     */
    @Test
    void testLoadVariablesInLists() {
        assertNotNull(testNoVar("synthetic/testlist.yml"));
    }

    /**
     * Tests injecting multiple molecules in the same shape.
     */
    @Test
    void testMultipleMolecules() {
        final Environment<?, ?> environment = testNoVar("synthetic/multiplemolecule.yml");
        environment.forEach(n -> assertEquals(4, n.getMoleculeCount()));
    }

    /**
     * Test loading layer classes.
     */
    @Test
    void testSingleValuedGeometricVar() {
        assertNotNull(testNoVar("synthetic/singleValuedGeometricVar.yml"));
    }

    /**
     * Test variables with same structure but different names.
     */
    @Test
    void testVariableContentClash() {
        assertNotNull(testNoVar("synthetic/varcontentclash.yml"));
    }

    /**
     * Test variables with same structure but different names.
     */
    @Test
    void testScalaVar() {
        final Environment<Object, ?> environment = testNoVar("synthetic/scalavar.yml");
        assertNotNull(environment);
        assertEquals(environment.makePosition(3, 10), environment.getPosition(environment.getNodeByID(0)));
    }

    /**
     * Test dependencies section.
     */
    @Test
    void testDependencies() {
        final var is = ResourceLoader.getResource("isac/16-dependencies.yaml");
        assertNotNull(is);
        final Loader loader = LoadAlchemist.from(is);
        final List<String> dependencies = loader.getRemoteDependencies();
        assertEquals(dependencies.size(), 2);
        assertEquals(dependencies.get(0), "dependencies_test.txt");
    }

    @Test
    void testMaxAliases() {
        assertFalse(testNoVar("yamlAliases/aliases.yml").getNodes().isEmpty());
    }

    private static <T, P extends Position<P>> Environment<T, P> testLoading(
            final InputStream resource,
            final Map<String, Double> vars
    ) {
        assertNotNull(resource, "Missing test resource " + resource);
        final Environment<T, P> environment = LoadAlchemist.from(resource, YamlProvider.INSTANCE)
                .<T, P>getWith(vars).getEnvironment();
        final Simulation<T, P> sim = new Engine<>(environment, 10_000);
        sim.play();
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
        sim.getError().ifPresent(e -> fail(e.getMessage()));
        return environment;
    }

    private static <T> Environment<T, ?> testNoVar(final InputStream resource) {
        return testLoading(resource, Collections.emptyMap());
    }

    private static <T> Environment<T, ?> testNoVar(final String resource) {
        return testLoading(ResourceLoader.getResourceAsStream(resource), Collections.emptyMap());
    }

}
