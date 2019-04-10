/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.actions.CellTensionPolarization;
import it.unibo.alchemist.model.implementations.conditions.TensionPresent;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.nodes.CircularDeformableCellImpl;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * 
 *
 */
public class TestDeformableCell {

    private static final double PRECISION = 0.0000000000001;
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
    private Environment<Double, Euclidean2DPosition> env;
    private CircularDeformableCell<Euclidean2DPosition> cellNode1;
    private CircularDeformableCell<Euclidean2DPosition> cellNode2;
    private CircularDeformableCell<Euclidean2DPosition> cellNode3;
    private CircularDeformableCell<Euclidean2DPosition> cellNode4;
    private CircularDeformableCell<Euclidean2DPosition> cellNode5;
    private final Incarnation<Double, Euclidean2DPosition> inc = new BiochemistryIncarnation<>();
    private RandomGenerator rand;
    private TimeDistribution<Double> time;

    /**
     * 
     */
    @Before
    public void setUp() {
        env = new BioRect2DEnvironmentNoOverlap(XMIN, XMAX, YMIN, YMAX);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        cellNode1 = new CircularDeformableCellImpl<>(env, 1, 1); // max rigidity
        cellNode2 = new CircularDeformableCellImpl<>(env, 1, 0.5);
        cellNode3 = new CircularDeformableCellImpl<>(env, 2, 0.5);
        cellNode4 = new CircularDeformableCellImpl<>(env, 3, 0.5);
        cellNode5 = new CircularDeformableCellImpl<>(env, 2, 0.5);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     * Testing if CircularDeformableCells are added correctly.
     */
    @Test
    public void testAddNode1() {
        env.addNode(cellNode1, CELL_POS1_1);
        env.addNode(cellNode2, CELL_POS1_2);
        env.addNode(cellNode3, CELL_POS1_3);
        env.addNode(cellNode4, CELL_POS1_4);

        assertNotNull("Position of cellNode2 = " + env.getPosition(cellNode2), env.getPosition(cellNode2));
        assertNotNull("Position of cellNode3 = " + env.getPosition(cellNode3), env.getPosition(cellNode3));
        assertNull("Position of cellNode4 = " + env.getPosition(cellNode3), env.getPosition(cellNode4));
    }

    /**
     * Testing if Environment updates correctly after node's remotion.
     */
    @Test
    public void testAddAndRemoveNode() {
        env.addNode(cellNode1, CELL_POS2_1);
        env.addNode(cellNode2, CELL_POS2_2);
        env.addNode(cellNode3, CELL_POS2_3);
        env.addNode(cellNode4, CELL_POS2_4);
        assertEquals(3d, ((EnvironmentSupportingDeformableCells<Euclidean2DPosition>) env).getMaxDiameterAmongCircularDeformableCells(), PRECISION);
        env.removeNode(cellNode1);
        env.removeNode(cellNode2);
        env.removeNode(cellNode3);
        env.removeNode(cellNode4);
        assertEquals(0d, ((EnvironmentSupportingDeformableCells<Euclidean2DPosition>) env).getMaxDiameterAmongCircularDeformableCells(), PRECISION);
    }

    /**
     * Testing {@link TensionPresent}.
     */
    @Test
    public void testTensionPresent1() {
        env.addNode(cellNode1, CELL_POS_TENSPRES1_1);
        env.addNode(cellNode2, CELL_POS_TENSPRES1_2);
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, "[] --> [A] if TensionPresent()"));
        assertFalse(cellNode1.getReactions().isEmpty());
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(1d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
        env.moveNodeToPosition(cellNode2, new Euclidean2DPosition(0, 4));
        assertFalse(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(0d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
    }

    /**
     * Testing {@link TensionPresent}.
     */
    @Test
    public void testTensionPresent2() {
        env.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        env.addNode(cellNode3, new Euclidean2DPosition(0, 1));
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, "[] --> [A] if TensionPresent()"));
        assertFalse(cellNode1.getReactions().isEmpty());
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(1d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
        env.moveNodeToPosition(cellNode3, MOVE_TO_POS2_1);
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(0.5, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
        env.moveNodeToPosition(cellNode3, MOVE_TO_POS2_2);
        assertFalse(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(0d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityContribution(),
                PRECISION);
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    public void testTensionPolarization1() {
        env.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        env.addNode(cellNode3, new Euclidean2DPosition(0, 1));
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, CELL_TENSION_POLARIZATION)); 
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(0, -1), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL1_1);
        cellNode1.setPolarization(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(0, -1), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL1_2);
        cellNode1.setPolarization(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(0, 0), cellNode1.getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    public void testTensionPolarization2() {
        env.addNode(cellNode1, new Euclidean2DPosition(0, 0)); 
        env.addNode(cellNode3, new Euclidean2DPosition(0, 1)); 
        env.addNode(cellNode2, MOVE_TO_POS_TENSPOL2_3);
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(0, 0), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL2_1);
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(0, 1), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL2_2);
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(0, 1), cellNode1.getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    public void testTensionPolarization3() {
        env.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        env.addNode(cellNode3, new Euclidean2DPosition(-1, 1));
        env.addNode(cellNode5, new Euclidean2DPosition(-1, -1));
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(1, 0), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, MOVE_TO_POS_TENSPOL3_1);
        cellNode1.setPolarization(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(cellNode1.getPolarizationVersor().getCoordinate(0), cellNode1.getPolarizationVersor().getCoordinate(0), PRECISION);
        env.moveNodeToPosition(cellNode3, new Euclidean2DPosition(-1, 1));
        env.moveNodeToPosition(cellNode5, MOVE_TO_POS_TENSPOL3_3);
        cellNode1.setPolarization(new Euclidean2DPosition(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(cellNode1.getPolarizationVersor().getCoordinate(0), -cellNode1.getPolarizationVersor().getCoordinate(1), PRECISION);
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    public void testTensionPolarization4() {
        env.addNode(cellNode3, new Euclidean2DPosition(0, 0));
        env.addNode(cellNode5, new Euclidean2DPosition(-1, 0));
        env.addNode(cellNode2, CELL_POS_TENSPOL4_1);
        cellNode3.addReaction(inc.createReaction(rand, env, cellNode3, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(1, 0), cellNode3.getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    public void testTensionPolarization5() {
        env.addNode(cellNode3, new Euclidean2DPosition(0, 0));
        env.addNode(cellNode5, new Euclidean2DPosition(-1, 0));
        env.addNode(cellNode2, CELL_POS_TENSPOL5_1);
        cellNode3.addReaction(inc.createReaction(rand, env, cellNode3, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(1, 0), cellNode3.getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}.
     */
    @Test
    public void testTensionPolarization6() {
        env.addNode(cellNode3, new Euclidean2DPosition(0, 0));
        env.addNode(cellNode4, CELL_POS_TENSPOL6_1);
        env.addNode(cellNode2, CELL_POS_TENSPOL6_2);
        cellNode3.addReaction(inc.createReaction(rand, env, cellNode3, time, CELL_TENSION_POLARIZATION));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Euclidean2DPosition(0, 0), cellNode3.getPolarizationVersor());
    }

    /**
     * Test if cell, in motion, stops when meets another cell.
     */
    @Test
    public void testMoveNode1() {
        env.addNode(cellNode1, new Euclidean2DPosition(0, 0));
        env.addNode(cellNode2, CELL_POS_MOV1);
        env.moveNodeToPosition(cellNode1, new Euclidean2DPosition(0, 10));
        assertEquals("Position of cellNode1 = " + env.getPosition(cellNode1), env.getPosition(cellNode1), EXPECTED_POS_MOV1);
    }
}
