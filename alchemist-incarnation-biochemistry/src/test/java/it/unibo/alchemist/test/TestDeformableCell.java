/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.actions.CellTensionPolarization;
import it.unibo.alchemist.model.conditions.TensionPresent;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.nodes.GenericNode;
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.properties.CircularDeformableCell;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.interfaces.properties.CircularDeformableCellProperty;
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 
 *
 */
class TestDeformableCell {

    private static final double PRECISION = 1e-13;
    private static final double XMIN = -10;
    private static final double XMAX = 10;
    private static final double YMIN = -10;
    private static final double YMAX = 10;
    private static final Euclidean2DPosition CELL_POS1_1 = new Euclidean2DPosition(0, 0);
    private static final Euclidean2DPosition CELL_POS1_2 = new Euclidean2DPosition(0, 0.75);
    private static final Euclidean2DPosition CELL_POS1_3 = new Euclidean2DPosition(0, -1);
    private static final Euclidean2DPosition CELL_POS1_4 = new Euclidean2DPosition(0,  0);
    private static final Euclidean2DPosition CELL_POS2_1 = new Euclidean2DPosition(0, 0);
    private static final Euclidean2DPosition CELL_POS2_2 = new Euclidean2DPosition(4, 4);
    private static final Euclidean2DPosition CELL_POS2_3 = new Euclidean2DPosition(0, -4);
    private static final Euclidean2DPosition CELL_POS2_4 = new Euclidean2DPosition(4, 0);
    private static final Euclidean2DPosition CELL_POS_TENSPRES1_1 = new Euclidean2DPosition(0, 0);
    private static final Euclidean2DPosition CELL_POS_TENSPRES1_2 = new Euclidean2DPosition(0, 0.75);
    private static final Euclidean2DPosition MOVE_TO_POS2_1 = new Euclidean2DPosition(0, 1.25);
    private static final Euclidean2DPosition MOVE_TO_POS2_2 = new Euclidean2DPosition(0, 1.5);
    private static final Euclidean2DPosition MOVE_TO_POS_TENSPOL1_1 = new Euclidean2DPosition(0, 1.25);
    private static final Euclidean2DPosition MOVE_TO_POS_TENSPOL1_2 = new Euclidean2DPosition(0, 1.5);
    private static final Euclidean2DPosition MOVE_TO_POS_TENSPOL2_3 = new Euclidean2DPosition(0, -0.75);
    private static final Euclidean2DPosition MOVE_TO_POS_TENSPOL2_1 = new Euclidean2DPosition(0, 1.25);
    private static final Euclidean2DPosition MOVE_TO_POS_TENSPOL2_2 = new Euclidean2DPosition(0, 1.5);
    private static final Euclidean2DPosition MOVE_TO_POS_TENSPOL3_1 = new Euclidean2DPosition(-1.5, 1.5);
    private static final Euclidean2DPosition MOVE_TO_POS_TENSPOL3_3 = new Euclidean2DPosition(-1.5, -1.5);
    private static final Euclidean2DPosition CELL_POS_TENSPOL4_1 = new Euclidean2DPosition(1.5, 0);
    private static final Euclidean2DPosition CELL_POS_TENSPOL5_1 = new Euclidean2DPosition(1.75, 0);
    private static final Euclidean2DPosition CELL_POS_TENSPOL6_1 = new Euclidean2DPosition(-4, 0);
    private static final Euclidean2DPosition CELL_POS_TENSPOL6_2 = new Euclidean2DPosition(1.75, 0);
    private static final Euclidean2DPosition CELL_POS_MOV1 = new Euclidean2DPosition(0, 5.75);
    private static final Euclidean2DPosition EXPECTED_POS_MOV1 = new Euclidean2DPosition(0, 5);
    private static final String CELL_TENSION_POLARIZATION = "[] --> [CellTensionPolarization()]";
    private Environment<Double, Euclidean2DPosition> environment;
    private Node<Double> cellNode1;
    private Node<Double> cellNode2;
    private Node<Double> cellNode3;
    private Node<Double> cellNode4;
    private Node<Double> cellNode5;
    private final BiochemistryIncarnation incarnation = new BiochemistryIncarnation();
    private RandomGenerator rand;
    private TimeDistribution<Double> time;

    private Node<Double> createDeformableCell(final double maxDiameter, final double rigidity) {
        final Node<Double> node =  new GenericNode<>(incarnation, environment);
        node.addProperty(
            new CircularDeformableCell(environment, node, maxDiameter, rigidity)
        );
        return node;
    }

    /**
     * 
     */
    @BeforeEach
    public void setUp() {
        environment = new BioRect2DEnvironmentNoOverlap(incarnation, XMIN, XMAX, YMIN, YMAX);
        environment.setLinkingRule(new ConnectWithinDistance<>(2));
        cellNode1 = createDeformableCell(1, 1); // max rigidity
        cellNode2 = createDeformableCell(1, 0.5);
        cellNode3 = createDeformableCell(2, 0.5);
        cellNode4 = createDeformableCell(3, 0.5);
        cellNode5 = createDeformableCell(2, 0.5);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     * Testing if CircularDeformableCells are added correctly.
     */
    @Test
    void testAddNode1() {
        environment.addNode(cellNode1, CELL_POS1_1);
        environment.addNode(cellNode2, CELL_POS1_2);
        environment.addNode(cellNode3, CELL_POS1_3);
        environment.addNode(cellNode4, CELL_POS1_4);

        assertNotNull(environment.getPosition(cellNode2), "Position of cellNode2 = " + environment.getPosition(cellNode2));
        assertNotNull(environment.getPosition(cellNode3), "Position of cellNode3 = " + environment.getPosition(cellNode3));
        assertFalse(environment.getNodes().contains(cellNode4), "unexpected node in the environment");
    }

    /**
     * Testing if Environment updates correctly after node's remotion.
     */
    @Test
    void testAddAndRemoveNode() {
        environment.addNode(cellNode1, CELL_POS2_1);
        environment.addNode(cellNode2, CELL_POS2_2);
        environment.addNode(cellNode3, CELL_POS2_3);
        environment.addNode(cellNode4, CELL_POS2_4);
        assertEquals(
                3d,
                ((EnvironmentSupportingDeformableCells<Euclidean2DPosition>) environment)
                        .getMaxDiameterAmongCircularDeformableCells(),
                PRECISION
        );
        environment.removeNode(cellNode1);
        environment.removeNode(cellNode2);
        environment.removeNode(cellNode3);
        environment.removeNode(cellNode4);
        assertEquals(0d,
                ((EnvironmentSupportingDeformableCells<Euclidean2DPosition>) environment)
                        .getMaxDiameterAmongCircularDeformableCells(),
                PRECISION
        );
    }

    /**
     * Testing {@link TensionPresent}.
     */
    @Test
    @SuppressWarnings("CPD-START")
    void testTensionPresent1() {
        environment.addNode(cellNode1, CELL_POS_TENSPRES1_1);
        environment.addNode(cellNode2, CELL_POS_TENSPRES1_2);
        cellNode1.addReaction(incarnation.createReaction(rand, environment, cellNode1, time, "[] --> [A] if TensionPresent()"));
        assertFalse(cellNode1.getReactions().isEmpty());
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).isValid());
        assertEquals(1d, cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
        environment.moveNodeToPosition(cellNode2, new Euclidean2DPosition(0, 4));
        assertFalse(cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).isValid());
        assertEquals(0d, cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
    }

    /**
     * Testing {@link TensionPresent}.
     */
    @Test
    void testTensionPresent2() {
        environment.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode3, new Euclidean2DPosition(0, 1));
        cellNode1.addReaction(incarnation.createReaction(rand, environment, cellNode1, time, "[] --> [A] if TensionPresent()"));
        assertFalse(cellNode1.getReactions().isEmpty());
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).isValid());
        assertEquals(1d, cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
        environment.moveNodeToPosition(cellNode3, MOVE_TO_POS2_1);
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).isValid());
        assertEquals(0.5, cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
        environment.moveNodeToPosition(cellNode3, MOVE_TO_POS2_2);
        assertFalse(cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).isValid());
        assertEquals(0d, cellNode1.getReactions().stream()
                .findFirst()
                .orElseThrow()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    void testTensionPolarization1() {
        environment.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode3, new Euclidean2DPosition(0, 1));
        cellNode1.addReaction(incarnation.createReaction(rand, environment, cellNode1, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(0, -1), cellNode1
                .asProperty(CircularDeformableCellProperty.class).getPolarizationVersor());
        environment.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL1_1);
        cellNode1.asProperty(CircularDeformableCellProperty.class)
                .setPolarizationVersor(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(0, -1), cellNode1
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
        environment.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL1_2);
        cellNode1.asProperty(CircularDeformableCellProperty.class)
                .setPolarizationVersor(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(0, 0), cellNode1
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    void testTensionPolarization2() {
        environment.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode3, new Euclidean2DPosition(0, 1));
        environment.addNode(cellNode2, MOVE_TO_POS_TENSPOL2_3);
        cellNode1.addReaction(incarnation.createReaction(rand, environment, cellNode1, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(0, 0), cellNode1
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
        environment.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL2_1);
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(0, 1), cellNode1
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
        environment.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL2_2);
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(0, 1), cellNode1
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    void testTensionPolarization3() {
        environment.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode3, new Euclidean2DPosition(-1, 1));
        environment.addNode(cellNode5, new Euclidean2DPosition(-1, -1));
        cellNode1.addReaction(incarnation.createReaction(rand, environment, cellNode1, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(1, 0), cellNode1
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
        environment.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL3_1);
        cellNode1.asProperty(CircularDeformableCellProperty.class)
                .setPolarizationVersor(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(
                cellNode1.asProperty(CircularDeformableCellProperty.class)
                        .getPolarizationVersor().getCoordinate(0),
                cellNode1.asProperty(CircularDeformableCellProperty.class)
                        .getPolarizationVersor().getCoordinate(0),
                PRECISION
        );
        environment.moveNodeToPosition(cellNode3, new Euclidean2DPosition(-1, 1));
        environment.moveNodeToPosition(cellNode5, MOVE_TO_POS_TENSPOL3_3);
        cellNode1.asProperty(CircularDeformableCellProperty.class)
                .setPolarizationVersor(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(
                cellNode1.asProperty(CircularDeformableCellProperty.class)
                        .getPolarizationVersor().getCoordinate(0),
                -cellNode1.asProperty(CircularDeformableCellProperty.class)
                        .getPolarizationVersor().getCoordinate(1),
                PRECISION
        );
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    void testTensionPolarization4() {
        environment.addNode(cellNode3, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode5, new Euclidean2DPosition(-1, 0));
        environment.addNode(cellNode2, CELL_POS_TENSPOL4_1);
        cellNode3.addReaction(incarnation.createReaction(rand, environment, cellNode3, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(1, 0), cellNode3
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    void testTensionPolarization5() {
        environment.addNode(cellNode3, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode5, new Euclidean2DPosition(-1, 0));
        environment.addNode(cellNode2, CELL_POS_TENSPOL5_1);
        cellNode3.addReaction(incarnation.createReaction(rand, environment, cellNode3, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(new Euclidean2DPosition(1, 0), cellNode3
                .asProperty(CircularDeformableCellProperty.class)
                .getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    void testTensionPolarization6() {
        environment.addNode(cellNode3, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode4, CELL_POS_TENSPOL6_1);
        environment.addNode(cellNode2, CELL_POS_TENSPOL6_2);
        cellNode3.addReaction(incarnation.createReaction(rand, environment, cellNode3, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .orElseThrow().execute();
        assertEquals(
            new Euclidean2DPosition(0, 0),
            cellNode3.asProperty(CircularDeformableCellProperty.class).getPolarizationVersor()
        );
    }

    /**
     * Test if cell, in motion, stops when meets another cell.
     */
    @Test
    void testMoveNode1() {
        environment.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        environment.addNode(cellNode2, CELL_POS_MOV1);
        environment.moveNodeToPosition(cellNode1, new Euclidean2DPosition(0, 10));
        assertEquals(
                environment.getPosition(cellNode1),
                EXPECTED_POS_MOV1,
                "Position of cellNode1 = " + environment.getPosition(cellNode1)
        );
    }
}
