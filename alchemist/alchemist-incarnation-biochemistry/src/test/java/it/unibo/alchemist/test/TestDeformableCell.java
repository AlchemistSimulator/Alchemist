package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.nodes.CircularDeformableCellImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Environment;
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
    private final Incarnation<Double> inc = new BiochemistryIncarnation();
    private RandomGenerator rand;
    private TimeDistribution<Double> time;

    /**
     * 
     */
    @Before
    public void setUp() {
        env = new BioRect2DEnvironmentNoOverlap();
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.EuclideanDistance<>(2));
        cellNode1 = new CircularDeformableCellImpl(env, 1, 0);
        cellNode2 = new CircularDeformableCellImpl(env, 1, 0.5);
        cellNode3 = new CircularDeformableCellImpl(env, 2, 0.5);
        cellNode4 = new CircularDeformableCellImpl(env, 3, 0.5);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     * 
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
     * 
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
