/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.nodes;

import it.unibo.alchemist.core.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation;
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.biochemistry.EnvironmentNode;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import it.unibo.alchemist.model.biochemistry.CircularCellProperty;
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test implementation of extra-cellular environment  created with EnvironmentNodes.
 *
 */

@SuppressWarnings("ALL")
final class TestEnvironmentNodes {

    private static final double PRECISION = 1e-12;
    private static final BiochemistryIncarnation INCARNATION = new BiochemistryIncarnation();
    private Environment<Double, Euclidean2DPosition> environment;
    private RandomGenerator rand;

    private void injectReaction(final String reaction, final Node<Double> destination, final double rate) {
        destination.addReaction(INCARNATION.createReaction(
                rand,
                environment,
                destination,
                new ExponentialTime<>(rate, rand),
                reaction
        ));
    }

    private void injectAInEnvReaction(final Node<Double> destination, final double rate) {
        injectReaction("[A] --> [A in env]", destination, rate);
    }

    private Node<Double> createNode() {
        return INCARNATION.createNode(rand, environment, null);
    }

    @BeforeEach
    public void setUp() {
        environment = new BioRect2DEnvironment(INCARNATION);
        rand = new MersenneTwister();
    }

    /**
     * test a simple reaction "[A] --> [A in env]".
     */
    @Test
    void test1() {
        final Node<Double> cellNode = createNode();
        final EnvironmentNode envNode = new EnvironmentNodeImpl(environment);
        final Molecule a = new Biomolecule("A");
        injectAInEnvReaction(cellNode, 1);
        cellNode.setConcentration(a, 1000.0);
        environment.setLinkingRule(new ConnectWithinDistance<>(2));
        environment.addNode(cellNode, new Euclidean2DPosition(0, 0));
        environment.addNode(envNode, new Euclidean2DPosition(0, 1));
        final Simulation<?, ?> sim = new Engine<>(environment, 10_000);
        sim.play();
        sim.run();
        assertEquals(envNode.getConcentration(a), 1000, PRECISION);
    }

    /**
     * test a simple reaction "[A] --> [A in env]".
     */
    @Test
    void test2() {
        final EnvironmentNode envNode1 = new EnvironmentNodeImpl(environment);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(environment);
        final Molecule a = new Biomolecule("A");
        injectAInEnvReaction(envNode1, 1);
        envNode1.setConcentration(a, 1000.0);
        environment.setLinkingRule(new ConnectWithinDistance<>(2));
        environment.addNode(envNode1, new Euclidean2DPosition(0, 0));
        environment.addNode(envNode2, new Euclidean2DPosition(0, 1));
        final Simulation<?, ?> sim = new Engine<>(environment, 10_000);
        sim.play();
        sim.run();
        assertTrue(envNode2.getConcentration(a) == 1000 && envNode1.getConcentration(a) == 0);
    }

    private Node<Double>[] populateWithNodes(final int count) {
        final Node<Double>[] result = new EnvironmentNodeImpl[count];
        for (int i = 0; i < result.length; i++) {
            result[i] = new EnvironmentNodeImpl(environment);
        }
        return result;
    }

    private Node<Double>[] populateSurroundingOrigin() {
        final Node<Double>[] nodes = populateWithNodes(4);
        environment.addNode(nodes[0], new Euclidean2DPosition(0, 1));
        environment.addNode(nodes[1], new Euclidean2DPosition(1, 0));
        environment.addNode(nodes[2], new Euclidean2DPosition(-1, 0));
        environment.addNode(nodes[3], new Euclidean2DPosition(0, -1));
        return nodes;
    }

    private void testDiffusion(final Node<Double> center) {
        environment.setLinkingRule(new ConnectWithinDistance<>(2));
        environment.addNode(center, new Euclidean2DPosition(0, 0));
        final Node<Double>[] nodes = populateSurroundingOrigin();
        final Molecule a = new Biomolecule("A");
        injectAInEnvReaction(center, 1);
        center.setConcentration(a, 1000.0);
        final Simulation<?, ?> sim = new Engine<>(environment, 10_000);
        sim.play();
        sim.run();
        assertEquals(0, center.getConcentration(a));
        assertTrue(Arrays.stream(nodes).noneMatch(it -> it.getConcentration(a) == 0));
    }

    /**
     * Test if env nodes are selected randomly.
     */
    @Test
    void testDiffusionWithEnvironmentNodes() {
        testDiffusion(new EnvironmentNodeImpl(environment));
    }

    /**
     * Test if env nodes with same concentration are selected randomly.
     */
    @Test
    void testDiffusionWithCellNodes() {
        testDiffusion(createNode());
    }
 
    /**
     * Test if env nodes with same concentration are selected randomly.
     */
    @Test
    void test5() {
        final Node<Double> cellNode = createNode();
        final EnvironmentNode envNode1 = new EnvironmentNodeImpl(environment);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(environment);
        final EnvironmentNode envNode3 = new EnvironmentNodeImpl(environment);
        final EnvironmentNode envNode4 = new EnvironmentNodeImpl(environment);
        final Molecule a = new Biomolecule("A");
        injectAInEnvReaction(cellNode, 1);
        injectAInEnvReaction(envNode1, 1000);
        injectAInEnvReaction(envNode2, 1000);
        final double total = 1000.0;
        cellNode.setConcentration(a, 1000.0);
        environment.setLinkingRule(new ConnectWithinDistance<>(1));
        final Euclidean2DPosition pos1 = new Euclidean2DPosition(0, -0.75);
        final Euclidean2DPosition pos2 = new Euclidean2DPosition(0, 0.75);
        final Euclidean2DPosition pos3 = new Euclidean2DPosition(0, 1.5);
        final Euclidean2DPosition pos4 = new Euclidean2DPosition(0, -1.5);
        environment.addNode(cellNode, new Euclidean2DPosition(0, 0));
        environment.addNode(envNode1, pos1);
        environment.addNode(envNode2, pos2);
        environment.addNode(envNode3, pos3);
        environment.addNode(envNode4, pos4);
        final Simulation<?, ?> sim = new Engine<>(environment, 10_000);
        sim.play();
        sim.run();
        assertNotEquals(0.0, envNode3.getConcentration(a));
        assertNotEquals(0.0, envNode4.getConcentration(a));
        assertEquals(total, envNode3.getConcentration(a) + envNode4.getConcentration(a), PRECISION);
    }

    /**
     * test a simple reaction "[A] --> [A in env]".
     */
    @Test
    void test6() {
        final Node<Double> cellNode = createNode();
        final Molecule a = new Biomolecule("A");
        injectAInEnvReaction(cellNode, 1);
        cellNode.setConcentration(a, 1000.0);
        environment.setLinkingRule(new ConnectWithinDistance<>(2));
        environment.addNode(cellNode, new Euclidean2DPosition(0, 0));
        final Simulation<?, ?> sim = new Engine<>(environment, 10_000);
        sim.play();
        sim.run();
        assertEquals(cellNode.getConcentration(a), 1000, PRECISION);
    }

    /**
     * Simple interaction between a CellNode and an EnviromentalNode.
     * Test transport of a molecule from cell to env.
     */
    @Test
    void testEnv1() {
        final var environment = testNoVar("testEnv1.yml");
        final double conA = (double) environment.getNodes().stream()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .findFirst()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertEquals(conA, 1000, Double.MIN_VALUE);
    }

    /**
     * Simple interaction between a CellNode and 4 EnviromentalNodes.
     */
    @Test
    void testEnv2() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv2.yml");
        final Node<Double> center = environment.getNodes().stream()
                .parallel()
                .filter(n -> n.asPropertyOrNull(CellProperty.class) != null)
                .findAny()
                .get();
        final double conAInNearest = environment.getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .min((n1, n2) -> Double.compare(
                        environment.getPosition(n1).distanceTo(environment.getPosition(center)),
                        environment.getPosition(n2).distanceTo(environment.getPosition(center))
                        ))
                .get().getConcentration(new Biomolecule("A"));
        assertEquals(conAInNearest, 1000, PRECISION);
    }

    /**
     * Simple interaction between a CellNode and an EnviromentalNode.
     * Test transport of a molecule from env to cell.
     */
    @Test
    void testEnv3() {
        final double conAInCell = (double) TestEnvironmentNodes.<Double, Euclidean2DPosition>testNoVar("testEnv3.yml")
            .getNodes()
            .stream()
            .filter(n -> n.asPropertyOrNull(CircularCellProperty.class) != null)
            .findAny()
            .get()
            .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) testNoVar("testEnv3.yml").getNodes().stream()
            .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
            .findAny()
            .get()
            .getConcentration(new Biomolecule("A"));
        assertEquals(conAInCell, 1000, PRECISION);
        assertEquals(conAInEnv, 0, PRECISION);
    }

    /**
     * The same as testEnv3, missing initial concentration of A in env.
     * So there's no A in env and in cell both.
     */
    @Test
    void testEnv4() {
        final double conAInCell = (double) TestEnvironmentNodes.<Double, Euclidean2DPosition>testNoVar("testEnv4.yml")
            .getNodes()
            .stream()
            .parallel()
            .filter(n -> n.asPropertyOrNull(CircularCellProperty.class) != null)
            .findAny()
            .get()
            .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) testNoVar("testEnv4.yml").getNodes().stream()
            .parallel()
            .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
            .findAny()
            .get()
            .getConcentration(new Biomolecule("A"));
        assertTrue(conAInCell == 0 && conAInEnv == 0);
    }

    /**
     * test programming an environment node.
     */
    @Test
    void testEnv5() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv5.yml");
        final double conAInEnv1 = (double) environment.getNodes().stream()
                .parallel()
                .filter(n -> environment.getPosition(n).equals(new Euclidean2DPosition(0, 0)))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv2 = (double) environment.getNodes().stream()
                .parallel()
                .filter(n -> environment.getPosition(n).equals(new Euclidean2DPosition(1, 0)))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue(conAInEnv1 == 0 && conAInEnv2 == 1000);
    }

    /**
     * test initialization of a junction between an EnvironmentNode and a CellNode.
     * Now this will throw an UnsupportedOperationException
     */
    @Test
    void testEnv6() {
        assertThrows(UnsupportedOperationException.class, () -> testNoVar("testEnv6.yml"));
    }

    /**
     * test if neighbors are selected correctly.
     */
    @Test
    void testEnv7() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv7.yml");
        final double conAInCell = (double) environment.getNodes().stream()
                .parallel()
                .filter(n -> n.asPropertyOrNull(CellProperty.class) != null)
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) environment.getNodes().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .mapToDouble(n -> n.getConcentration(new Biomolecule("A")))
                .sum();
        final double expectedConcInCell = 2000;
        final double expectedCOncInEnv = 0;
        assertTrue(conAInCell == expectedConcInCell && conAInEnv == expectedCOncInEnv);
    }

    /**
     * test if neighbors are selected correctly.
     */
    @Test
    void testEnv8() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv8.yml");
        final double conAInCell = (double) environment.getNodes().stream()
                .parallel()
                .filter(n -> n.asPropertyOrNull(CellProperty.class) != null)
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertEquals(conAInCell, 1000, PRECISION);
    }

    private static <T, P extends Position<P>> Environment<T, P> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }

    private static <T, P extends Position<P>> Environment<T, P> testLoading(
            final String resource,
            final Map<String, Double> vars
    ) {
        final var res = ResourceLoader.getResource(resource);
        assertNotNull(res);
        final Environment<T, P> environment = LoadAlchemist.from(res).<T, P>getWith(vars).getEnvironment();
        final Simulation<?, ?> sim = new Engine<>(environment, 10_000);
        sim.play();
        sim.run();
        return environment;
    }

}
