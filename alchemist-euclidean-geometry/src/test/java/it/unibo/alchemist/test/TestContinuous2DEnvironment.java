/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.model.SupportedIncarnations;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 */
final class TestContinuous2DEnvironment {

    private static final double [] ZEROS = {0, 0};
    private static final double [] P2_3 = {2, 3};
    private static final double [] P2_2 = {2, 2};
    private static final double TOLERANCE = 1E-15;
    private static final Incarnation<Integer, Euclidean2DPosition> INCARNATION =
        SupportedIncarnations.<Integer, Euclidean2DPosition>get("protelis").orElseThrow();
    private Continuous2DEnvironment<Integer> environment;

    /**
     * Instances the environment.
     */
    @BeforeEach
    public void setUp() {
        environment = new Continuous2DEnvironment<>(INCARNATION);
        environment.setLinkingRule(new NoLinks<>());
    }

    private Node<Integer> createIntNode(
            final Incarnation<Integer, Euclidean2DPosition> incarnation,
            final Environment<Integer, Euclidean2DPosition> environment
    ) {
        return new GenericNode<>(incarnation, environment) {
            @Override
            public Integer createT() {
                return 0;
            }
        };
    }

    /**
     * Test size initialization and change.
     */
    @Test
    void testEnvironmentSize() {
        assertEquals(0, environment.getNodeCount());
        assertArrayEquals(ZEROS, environment.getSize(), TOLERANCE);
        environment.addNode(createIntNode(INCARNATION, environment), new Euclidean2DPosition(P2_3));
        assertArrayEquals(ZEROS, environment.getSize(), TOLERANCE);
        environment.addNode(createIntNode(INCARNATION, environment), new Euclidean2DPosition(P2_2));
        assertArrayEquals(new double[]{0, 1}, environment.getSize(), TOLERANCE);
        environment.addNode(createIntNode(INCARNATION, environment), new Euclidean2DPosition(ZEROS));
        assertArrayEquals(P2_3, environment.getSize(), TOLERANCE);
    }

    /**
     * Test environment offset.
     */
    @Test
    void testEnvironmentOffset() {
        assertEquals(0, environment.getNodeCount());
        assertTrue(Double.isNaN(environment.getOffset()[0]));
        assertTrue(Double.isNaN(environment.getOffset()[1]));
        environment.addNode(createIntNode(INCARNATION, environment), new Euclidean2DPosition(P2_3));
        assertArrayEquals(P2_3, environment.getOffset(), TOLERANCE);
        environment.addNode(createIntNode(INCARNATION, environment), new Euclidean2DPosition(P2_2));
        assertArrayEquals(P2_2, environment.getOffset(), TOLERANCE);
        environment.addNode(createIntNode(INCARNATION, environment), new Euclidean2DPosition(ZEROS));
        assertArrayEquals(ZEROS, environment.getOffset(), TOLERANCE);
    }

    /**
     * Test failure on wrong queries.
     */
    @Test
    void testNegativeRangeQuery() {
        assertEquals(0, environment.getNodeCount());
        final Node<Integer> dummy = createIntNode(INCARNATION, environment);
        environment.addNode(dummy, new Euclidean2DPosition(ZEROS));
        try {
            environment.getNodesWithinRange(dummy, -1);
            fail();
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * Test failure on wrong queries.
     */
    @Test
    void testZeroRangeQuery() {
        assertEquals(0, environment.getNodeCount());
        final Node<Integer> dummy = createIntNode(INCARNATION, environment);
        final Node<Integer> dummy2 = createIntNode(INCARNATION, environment);
        environment.addNode(dummy, new Euclidean2DPosition(ZEROS));
        environment.addNode(dummy2, new Euclidean2DPosition(ZEROS));
        assertEquals(2, environment.getNodeCount());
        assertEquals(Collections.singletonList(dummy2), environment.getNodesWithinRange(dummy, Math.nextUp(0)));
    }

}
