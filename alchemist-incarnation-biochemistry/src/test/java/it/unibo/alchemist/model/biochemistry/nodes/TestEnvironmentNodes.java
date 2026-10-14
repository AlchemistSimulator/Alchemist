/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.nodes;

import it.unibo.alchemist.boundary.LoadAlchemist;
import it.unibo.alchemist.core.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import it.unibo.alchemist.model.biochemistry.CircularCellProperty;
import it.unibo.alchemist.model.biochemistry.EnvironmentNode;
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static it.unibo.alchemist.model.biochemistry.util.Nodes.getOrZeroConcentration;
import static it.unibo.alchemist.test.AlchemistTesting.terminatingAfterSteps;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test implementation of extracellular environment created with EnvironmentNodes.
 *
 */
final class TestEnvironmentNodes {

    private static final String A = "A";
    private static final long FINAL_STEP = 10_000;
    private static final BiochemistryIncarnation INCARNATION = new BiochemistryIncarnation();
    private static final double PRECISION = 1e-12;
    private RandomGenerator rand;
    private Environment<Double, Euclidean2DPosition> standardEnvironment;

    @BeforeEach
    public void setUp() {
        standardEnvironment = new BioRect2DEnvironment(INCARNATION);
        rand = new MersenneTwister();
    }

    /**
     * A cell absorbing molecules from the environment takes them all from the neighboring environment node.
     */
    @Test
    void cellAbsorbsMoleculeFromNeighboringEnvironmentNode() {
        final double conAInCell = TestEnvironmentNodes.<Double, Euclidean2DPosition>testNoVar("testEnv3.yml")
            .getNodes().getCurrent()
            .stream()
            .filter(n -> n.asPropertyOrNull(CircularCellProperty.class) != null)
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        final double conAInEnv = TestEnvironmentNodes.<Double, Euclidean2DPosition>testNoVar("testEnv3.yml")
            .getNodes().getCurrent()
            .stream()
            .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        assertEquals(1000, conAInCell, PRECISION);
        assertEquals(0, conAInEnv, PRECISION);
    }

    /**
     * A cell absorbing molecules from an environment node holding none of them gets none.
     */
    @Test
    void cellAbsorbsNothingFromEmptyEnvironmentNode() {
        final double conAInCell = TestEnvironmentNodes.<Double, Euclidean2DPosition>testNoVar("testEnv4.yml")
            .getNodes().getCurrent()
            .stream()
            .parallel()
            .filter(n -> n.asPropertyOrNull(CircularCellProperty.class) != null)
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        final double conAInEnv = TestEnvironmentNodes.<Double, Euclidean2DPosition>testNoVar("testEnv4.yml")
            .getNodes().getCurrent()
            .stream()
            .parallel()
            .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        assertTrue(conAInCell == 0 && conAInEnv == 0);
    }

    /**
     * A cell sending molecules to its neighbors sends none to the neighboring environment nodes.
     */
    @SuppressWarnings("unchecked")
    @Test
    void cellDoesNotSendMoleculesToNeighboringEnvironmentNodes() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv8.yml");
        final double conAInCell = environment.getNodes().getCurrent().stream()
            .parallel()
            .filter(n -> n.asPropertyOrNull(CellProperty.class) != null)
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        assertEquals(1000, conAInCell, PRECISION);
    }

    /**
     * A cell releasing its molecules moves them all to the nearest of the neighboring environment nodes.
     */
    @SuppressWarnings("unchecked")
    @Test
    void cellReleasesMoleculeIntoNearestEnvironmentNode() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv2.yml");
        final Node<Double> center = environment.getNodes().getCurrent().stream()
            .parallel()
            .filter(n -> n.asPropertyOrNull(CellProperty.class) != null)
            .findAny()
            .orElseThrow();
        final double conAInNearest = environment.getNodes().getCurrent().stream()
            .parallel()
            .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
            .min(
                Comparator.comparingDouble(n ->
                    environment.getCurrentPosition(n).distanceTo(environment.getCurrentPosition(center))
                )
            )
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        assertEquals(1000, conAInNearest, PRECISION);
    }

    /**
     * A cell releasing its molecules into the environment moves them all to the neighboring environment node.
     */
    @Test
    void cellReleasesMoleculeIntoNeighboringEnvironmentNode() {
        final Node<Double> cellNode = createNode();
        final EnvironmentNode envNode = new EnvironmentNodeImpl(standardEnvironment);
        final Molecule a = new Biomolecule(A);
        injectAInEnvReaction(cellNode, 1);
        cellNode.setConcentration(a, 1000.0);
        standardEnvironment.setLinkingRule(new ConnectWithinDistance<>(2));
        standardEnvironment.addNode(cellNode, new Euclidean2DPosition(0, 0));
        standardEnvironment.addNode(envNode, new Euclidean2DPosition(0, 1));
        simulate(standardEnvironment);
        assertEquals(1000, getOrZeroConcentration(envNode, a), PRECISION);
    }

    /**
     * A cell releasing its molecules spreads them to every surrounding environment node.
     */
    @Test
    void cellSpreadsMoleculeToEverySurroundingEnvironmentNode() {
        assertSpreadsToSurroundingNodes(createNode());
    }

    /**
     * A cell releasing its molecules into the environment keeps them when no environment node neighbors it.
     */
    @Test
    void cellWithoutEnvironmentNodesKeepsMolecule() {
        final Node<Double> cellNode = createNode();
        final Molecule a = new Biomolecule(A);
        injectAInEnvReaction(cellNode, 1);
        cellNode.setConcentration(a, 1000.0);
        standardEnvironment.setLinkingRule(new ConnectWithinDistance<>(2));
        standardEnvironment.addNode(cellNode, new Euclidean2DPosition(0, 0));
        simulate(standardEnvironment);
        assertEquals(1000, getOrZeroConcentration(cellNode, a), PRECISION);
    }

    /**
     * An environment node releasing its molecules moves them all to the neighboring environment node.
     */
    @Test
    void environmentNodeReleasesMoleculeIntoNeighboringEnvironmentNode() {
        final EnvironmentNode envNode1 = new EnvironmentNodeImpl(standardEnvironment);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(standardEnvironment);
        final Molecule a = new Biomolecule(A);
        injectAInEnvReaction(envNode1, 1);
        envNode1.setConcentration(a, 1000.0);
        standardEnvironment.setLinkingRule(new ConnectWithinDistance<>(2));
        standardEnvironment.addNode(envNode1, new Euclidean2DPosition(0, 0));
        standardEnvironment.addNode(envNode2, new Euclidean2DPosition(0, 1));
        simulate(standardEnvironment);
        assertTrue(getOrZeroConcentration(envNode2, a) == 1000 && getOrZeroConcentration(envNode1, a) == 0);
    }

    /**
     * An environment node releasing its molecules spreads them to every surrounding environment node.
     */
    @Test
    void environmentNodeSpreadsMoleculeToEverySurroundingEnvironmentNode() {
        assertSpreadsToSurroundingNodes(new EnvironmentNodeImpl(standardEnvironment));
    }

    /**
     * Junctions between an environment node and a cell are unsupported.
     */
    @Test
    void junctionsWithEnvironmentNodesAreUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> testNoVar("testEnv6.yml"));
    }

    /**
     * A loaded cell releasing its molecules moves them all to the neighboring environment node.
     */
    @Test
    void loadedCellReleasesMoleculeIntoEnvironmentNode() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv1.yml");
        final double conA = environment.getNodes().getCurrent().stream()
            .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
            .findFirst()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        assertEquals(1000, conA, Double.MIN_VALUE);
    }

    /**
     * A loaded environment node releasing its molecules moves them all to the neighboring environment node.
     */
    @Test
    void loadedEnvironmentNodeReleasesMoleculeIntoNeighboringEnvironmentNode() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv5.yml");
        final double conAInEnv1 = environment.getNodes().getCurrent().stream()
            .parallel()
            .filter(n -> environment.getCurrentPosition(n).equals(new Euclidean2DPosition(0, 0)))
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        final double conAInEnv2 = environment.getNodes().getCurrent().stream()
            .parallel()
            .filter(n -> environment.getCurrentPosition(n).equals(new Euclidean2DPosition(1, 0)))
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        assertTrue(conAInEnv1 == 0 && conAInEnv2 == 1000);
    }

    /**
     * Molecules released by a cell flow through the environment nodes forwarding them, reaching both the outer environment
     * nodes, with no loss.
     */
    @Test
    void moleculeReleasedByCellFlowsToOuterEnvironmentNodesWithoutLoss() {
        final Node<Double> cellNode = createNode();
        final EnvironmentNode envNode1 = new EnvironmentNodeImpl(standardEnvironment);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(standardEnvironment);
        final EnvironmentNode envNode3 = new EnvironmentNodeImpl(standardEnvironment);
        final EnvironmentNode envNode4 = new EnvironmentNodeImpl(standardEnvironment);
        final Molecule a = new Biomolecule(A);
        injectAInEnvReaction(cellNode, 1);
        injectAInEnvReaction(envNode1, 1000);
        injectAInEnvReaction(envNode2, 1000);
        final double total = 1000.0;
        cellNode.setConcentration(a, 1000.0);
        standardEnvironment.setLinkingRule(new ConnectWithinDistance<>(1));
        final Euclidean2DPosition pos1 = new Euclidean2DPosition(0, -0.75);
        final Euclidean2DPosition pos2 = new Euclidean2DPosition(0, 0.75);
        final Euclidean2DPosition pos3 = new Euclidean2DPosition(0, 1.5);
        final Euclidean2DPosition pos4 = new Euclidean2DPosition(0, -1.5);
        standardEnvironment.addNode(cellNode, new Euclidean2DPosition(0, 0));
        standardEnvironment.addNode(envNode1, pos1);
        standardEnvironment.addNode(envNode2, pos2);
        standardEnvironment.addNode(envNode3, pos3);
        standardEnvironment.addNode(envNode4, pos4);
        simulate(standardEnvironment);
        assertNotEquals(0.0, getOrZeroConcentration(envNode3, a));
        assertNotEquals(0.0, getOrZeroConcentration(envNode4, a));
        assertEquals(total, getOrZeroConcentration(envNode3, a) + getOrZeroConcentration(envNode4, a), PRECISION);
    }

    /**
     * Molecules sent to neighbors gather in the cell: environment nodes send theirs to the cell, which sends none to
     * them.
     */
    @SuppressWarnings("unchecked")
    @Test
    void moleculesSentToNeighborsGatherInCellRatherThanEnvironmentNodes() {
        final Environment<Double, Euclidean2DPosition> environment = testNoVar("testEnv7.yml");
        final double conAInCell = environment.getNodes().getCurrent().stream()
            .parallel()
            .filter(n -> n.asPropertyOrNull(CellProperty.class) != null)
            .findAny()
            .map(node -> getOrZeroConcentration(node, new Biomolecule(A)))
            .orElseThrow();
        final double conAInEnv = environment.getNodes().getCurrent().stream()
            .parallel()
            .filter(n -> n instanceof EnvironmentNode)
            .mapToDouble(n -> getOrZeroConcentration(n, new Biomolecule(A)))
            .sum();
        final double expectedConcInCell = 2000;
        final double expectedCOncInEnv = 0;
        assertTrue(conAInCell == expectedConcInCell && conAInEnv == expectedCOncInEnv);
    }

    private void assertSpreadsToSurroundingNodes(final Node<Double> center) {
        standardEnvironment.setLinkingRule(new ConnectWithinDistance<>(2));
        standardEnvironment.addNode(center, new Euclidean2DPosition(0, 0));
        final Node<Double>[] nodes = populateSurroundingOrigin();
        final Molecule a = new Biomolecule(A);
        injectAInEnvReaction(center, 1);
        center.setConcentration(a, 1000.0);
        simulate(standardEnvironment);
        assertEquals(0, getOrZeroConcentration(center, a));
        assertTrue(Arrays.stream(nodes).noneMatch(it -> getOrZeroConcentration(it, a) == 0));
    }

    private Node<Double> createNode() {
        return INCARNATION.createNode(rand, standardEnvironment, null);
    }

    private void injectAInEnvReaction(final Node<Double> destination, final double rate) {
        injectReaction("[A] --> [A in env]", destination, rate);
    }

    private void injectReaction(final String reaction, final Node<Double> destination, final double rate) {
        destination.addReaction(INCARNATION.createReaction(
                rand,
            standardEnvironment,
                destination,
                new ExponentialTime<>(rate, rand),
                reaction
        ));
    }

    private Node<Double>[] populateSurroundingOrigin() {
        final Node<Double>[] nodes = populateWithNodes(4);
        standardEnvironment.addNode(nodes[0], new Euclidean2DPosition(0, 1));
        standardEnvironment.addNode(nodes[1], new Euclidean2DPosition(1, 0));
        standardEnvironment.addNode(nodes[2], new Euclidean2DPosition(-1, 0));
        standardEnvironment.addNode(nodes[3], new Euclidean2DPosition(0, -1));
        return nodes;
    }

    private Node<Double>[] populateWithNodes(final int count) {
        final Node<Double>[] result = new EnvironmentNodeImpl[count];
        for (int i = 0; i < result.length; i++) {
            result[i] = new EnvironmentNodeImpl(standardEnvironment);
        }
        return result;
    }

    private static <T, P extends Position<P>> void simulate(final Environment<T, P> environment) {
        simulate(new Engine<>(environment));
    }

    private static <T, P extends Position<P>> void simulate(final Simulation<T, P> simulation) {
        terminatingAfterSteps(simulation, FINAL_STEP);
        simulation.play();
        simulation.run();
    }

    private static <T, P extends Position<P>> Environment<T, P> testLoading(
            final String resource,
            final Map<String, Double> vars
    ) {
        final var res = ResourceLoader.getResource(resource);
        assertNotNull(res);
        final var simulation = LoadAlchemist.from(res).<T, P>getWith(vars);
        simulate(simulation);
        return simulation.getEnvironment();
    }

    private static <T, P extends Position<P>> Environment<T, P> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }
}
