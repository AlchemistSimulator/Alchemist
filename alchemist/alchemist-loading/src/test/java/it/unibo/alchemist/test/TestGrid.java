package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import it.unibo.alchemist.loader.displacements.Grid;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;

/**
 *
 */
public class TestGrid {
    private static final double[] X = {1, 10, 0, 1, 10, -10};
    private static final double[] Y = {9.9, 1, 0, 1, 10, -10};
    private static final long[] EXPECTED = {9, 10, 0, 1, 100, 100};

    /**
     * 
     */
    @Test
    public void testVerticalLine() {
        test(EXPECTED[0], X[0], Y[0]);
        assertEquals(10L, new Grid(new Continuous2DEnvironment<>(), new MersenneTwister(), 0, 0, 1, 10, 1, 1, 0, 0).stream().count());
    }

    /**
     * 
     */
    @Test
    public void testHorizontalLine() {
        test(EXPECTED[1], X[1], Y[1]);
    }

    /**
     * 
     */
    @Test
    public void testEmpty() {
        test(EXPECTED[2], X[2], Y[2]);
    }

    /**
     * 
     */
    @Test
    public void test1x1() {
        test(EXPECTED[3], X[3], Y[3]);
    }

    /**
     * 
     */
    @Test
    public void test10x10() {
        test(EXPECTED[4], X[4], Y[4]);
    }

    /**
     * 
     */
    @Test
    public void test10x10negative() {
        test(EXPECTED[5], X[5], Y[5]);
    }
    
    private void test(long expected, double x, double y) {
        assertEquals(expected, new Grid(new Continuous2DEnvironment<>(), new MersenneTwister(), 0, 0, x, y, 1, 1, 0, 0).stream().count());
    }
    
}
