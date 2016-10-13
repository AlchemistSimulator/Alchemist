package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.swing.event.CellEditorListener;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.actions.CellTensionPolarization;
import it.unibo.alchemist.model.implementations.conditions.TensionPresent;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.nodes.CircularDeformableCellImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
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

    private Environment<Double> env;
    private CircularDeformableCell cellNode1;
    private CircularDeformableCell cellNode2;
    private CircularDeformableCell cellNode3;
    private CircularDeformableCell cellNode4;
    private CircularDeformableCell cellNode5;
    private final Incarnation<Double> inc = new BiochemistryIncarnation();
    private RandomGenerator rand;
    private TimeDistribution<Double> time;

    /**
     * 
     */
    @Before
    public void setUp() {
        //CHECKSTYLE:OFF: MagicNumber
        env = new BioRect2DEnvironmentNoOverlap(-10, 10, -10, 10);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.EuclideanDistance<>(2));
        cellNode1 = new CircularDeformableCellImpl(env, 1, 1); // max rigidity
        cellNode2 = new CircularDeformableCellImpl(env, 1, 0.5);
        cellNode3 = new CircularDeformableCellImpl(env, 2, 0.5);
        cellNode4 = new CircularDeformableCellImpl(env, 3, 0.5);
        cellNode5 = new CircularDeformableCellImpl(env, 2, 0.5);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     * Testing if CircularDeformableCells are added correctly
     */
    @Test
    public void testAddNode1() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode2, new Continuous2DEuclidean(0, 0.75));
        env.addNode(cellNode3, new Continuous2DEuclidean(0, -1));
        env.addNode(cellNode4, new Continuous2DEuclidean(0,  0));

        assertNotNull("Position of cellNode2 = " + env.getPosition(cellNode2), env.getPosition(cellNode2));
        assertNotNull("Position of cellNode3 = " + env.getPosition(cellNode3), env.getPosition(cellNode3));
        assertNull("Position of cellNode4 = " + env.getPosition(cellNode3), env.getPosition(cellNode4));
    }
    
    /**
     * Testing if Environment updates correctly after node's remotion
     */
    @Test
    public void testAddAndRemoveNode() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode2, new Continuous2DEuclidean(4, 4));
        env.addNode(cellNode3, new Continuous2DEuclidean(0, -4));
        env.addNode(cellNode4, new Continuous2DEuclidean(4, 0));
        assertEquals(3d, ((EnvironmentSupportingDeformableCells) env).getMaxDiameterAmongDeformableCells(), 0.0000000000001);
        env.removeNode(cellNode1);
        env.removeNode(cellNode2);
        env.removeNode(cellNode3);
        env.removeNode(cellNode4);
        assertEquals(0d, ((EnvironmentSupportingDeformableCells) env).getMaxDiameterAmongDeformableCells(), 0.0000000000001);
    }
    
    /**
     * Testing {@link TensionPresent}
     */
    @Test
    public void testTensionPresent1() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode2, new Continuous2DEuclidean(0, 0.75));
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, "[] --> [A] if TensionPresent()"));
        assertFalse(cellNode1.getReactions().isEmpty());
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(1d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityConditioning(), 
                0.0000000001);
        env.moveNodeToPosition(cellNode2, new Continuous2DEuclidean(0, 4));
        assertFalse(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(0d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityConditioning(), 
                0.0000000001);
    }
    
    /**
     * Testing {@link TensionPresent}
     */
    @Test
    public void testTensionPresent2() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode3, new Continuous2DEuclidean(0, 1));
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, "[] --> [A] if TensionPresent()"));
        assertFalse(cellNode1.getReactions().isEmpty());
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(1d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityConditioning(), 
                0.0000000001);
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(0, 1.25));
        assertTrue(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(0.5, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityConditioning(), 
                0.0000000001);
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(0, 1.5));
        assertFalse(cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).isValid());
        assertEquals(0d, cellNode1.getReactions().stream()
                .findFirst()
                .get()
                .getConditions().get(0).getPropensityConditioning(), 
                0.0000000001);
    }
    
    /**
     * Testing {@link CellTensionPolarization}
     */
    @Test
    public void testTensionPolarization1() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode3, new Continuous2DEuclidean(0, 1));
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, "[] --> [CellTensionPolarization()]")); //NOPMD
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(0, -1), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(0, 1.25));
        cellNode1.setPolarization(new Continuous2DEuclidean(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(0, -1), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(0, 1.5));
        cellNode1.setPolarization(new Continuous2DEuclidean(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(0, 0), cellNode1.getPolarizationVersor());
    }
    
    /**
     * Testing {@link CellTensionPolarization}
     */
    @Test
    public void testTensionPolarization2() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0)); // 1 1
        env.addNode(cellNode3, new Continuous2DEuclidean(0, 1)); // 2 1
        env.addNode(cellNode2, new Continuous2DEuclidean(0, -0.75)); // 1 0.5
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, "[] --> [CellTensionPolarization()]"));
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(0, 0), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(0, 1.25));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(0, 1), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(0, 1.5));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(0, 1), cellNode1.getPolarizationVersor());
    }
    
    /**
     * Testing {@link CellTensionPolarization}
     */
    @Test
    public void testTensionPolarization3() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode3, new Continuous2DEuclidean(-1, 1));
        env.addNode(cellNode5, new Continuous2DEuclidean(-1, -1));
        cellNode1.addReaction(inc.createReaction(rand, env, cellNode1, time, "[] --> [CellTensionPolarization()]"));
        assertFalse(cellNode1.getReactions().isEmpty());
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(1, 0), cellNode1.getPolarizationVersor());
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(-1.5, 1.5));
        cellNode1.setPolarization(new Continuous2DEuclidean(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(cellNode1.getPolarizationVersor().getCoordinate(0), cellNode1.getPolarizationVersor().getCoordinate(0), 0.0001);
        env.moveNodeToPosition(cellNode3, new Continuous2DEuclidean(-1, 1));
        env.moveNodeToPosition(cellNode5, new Continuous2DEuclidean(-1.5, -1.5));
        cellNode1.setPolarization(new Continuous2DEuclidean(0, 0));
        cellNode1.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(cellNode1.getPolarizationVersor().getCoordinate(0), -cellNode1.getPolarizationVersor().getCoordinate(1), 0.0001);
    }

    /**
     * Testing {@link CellTensionPolarization}
     */
    @Test
    public void testTensionPolarization4() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode3, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode5, new Continuous2DEuclidean(-1, 0));
        env.addNode(cellNode2, new Continuous2DEuclidean(1.5, 0));
        cellNode3.addReaction(inc.createReaction(rand, env, cellNode3, time, "[] --> [CellTensionPolarization()]"));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(1, 0), cellNode3.getPolarizationVersor());
    }

    /**
     * Testing {@link CellTensionPolarization}
     */
    @Test
    public void testTensionPolarization5() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode3, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode5, new Continuous2DEuclidean(-1, 0));
        env.addNode(cellNode2, new Continuous2DEuclidean(1.75, 0));
        cellNode3.addReaction(inc.createReaction(rand, env, cellNode3, time, "[] --> [CellTensionPolarization()]"));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(1, 0), cellNode3.getPolarizationVersor());
    }
    
    /**
     * Testing {@link CellTensionPolarization}
     */
    @Test
    public void testTensionPolarization6() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode3, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode4, new Continuous2DEuclidean(-4, 0));
        env.addNode(cellNode2, new Continuous2DEuclidean(1.75, 0));
        cellNode3.addReaction(inc.createReaction(rand, env, cellNode3, time, "[] --> [CellTensionPolarization()]"));
        assertFalse(cellNode3.getReactions().isEmpty());
        cellNode3.getReactions().stream()
        .findFirst()
        .get().execute();
        assertEquals(new Continuous2DEuclidean(0, 0), cellNode3.getPolarizationVersor());
    }
    
    /**
     * Test if cell, in motion, stops when meets another cell.
     */
    @Test
    public void testMoveNode1() {
        //CHECKSTYLE:OFF: MagicNumber
        env.addNode(cellNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(cellNode2, new Continuous2DEuclidean(0, 5.75));
        env.moveNodeToPosition(cellNode1, new Continuous2DEuclidean(0, 10));

        assertEquals("Position of cellNode1 = " + env.getPosition(cellNode1), env.getPosition(cellNode1), new Continuous2DEuclidean(0, 5));
    }
    
}
