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

    /**
     * 
     */
    @Test
    public void testVerticalLine() {
        test(9, 1, 9.9);
        assertEquals(10L, new Grid(new Continuous2DEnvironment<>(), new MersenneTwister(), 0, 0, 1, 10, 1, 1, 0, 0).stream().count());
    }

    /**
     * 
     */
    @Test
    public void testHorizontalLine() {
        test(10, 10, 1);
    }

    /**
     * 
     */
    @Test
    public void testEmpty() {
        test(0, 0, 0);
    }

    /**
     * 
     */
    @Test
    public void test1x1() {
        test(1, 1, 1);
    }

    /**
     * 
     */
    @Test
    public void test10x10() {
        test(100, 10, 10);
    }

    /**
     * 
     */
    @Test
    public void test10x10negative() {
        test(100, -10, -10);
    }

    private void test(final long expected, final double x, final double y) {
        assertEquals(expected, new Grid(new Continuous2DEnvironment<>(), new MersenneTwister(), 0, 0, x, y, 1, 1, 0, 0).stream().count());
    }

}
