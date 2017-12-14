package it.unibo.alchemist.test;

import it.unibo.alchemist.loader.displacements.Grid;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestGrid {
    // Test values to avoid using magic numbers
    // Arrays of test values
    private static final double[] X = {1, 10, 0, 1, 10, -10};
    private static final double[] Y = {9.9, 1, 0, 1, 10, -10};
    private static final long[] EXPECTED = {9, 10, 0, 1, 100, 100};
    // Indexes
    private static final int VERTICAL = 0;
    private static final int HORIZONTAL = 1;
    private static final int EMPTY = 2;
    private static final int ONE_X_ONE = 3;
    private static final int TEN_X_TEN = 4;
    private static final int NEGATIVE_10_X_10 = 5;

    /**
     *
     */
    @Test
    public void testVerticalLine() {
        test(EXPECTED[VERTICAL], X[VERTICAL], Y[VERTICAL]);
        assertEquals(10L, new Grid(new Continuous2DEnvironment<>(), new MersenneTwister(), 0, 0, 1, 10, 1, 1, 0, 0).stream().count());
    }

    /**
     *
     */
    @Test
    public void testHorizontalLine() {
        test(EXPECTED[HORIZONTAL], X[HORIZONTAL], Y[HORIZONTAL]);
    }

    /**
     *
     */
    @Test
    public void testEmpty() {
        test(EXPECTED[EMPTY], X[EMPTY], Y[EMPTY]);
    }

    /**
     *
     */
    @Test
    public void test1x1() {
        test(EXPECTED[ONE_X_ONE], X[ONE_X_ONE], Y[ONE_X_ONE]);
    }

    /**
     *
     */
    @Test
    public void test10x10() {
        test(EXPECTED[TEN_X_TEN], X[TEN_X_TEN], Y[TEN_X_TEN]);
    }

    /**
     *
     */
    @Test
    public void test10x10negative() {
        test(EXPECTED[NEGATIVE_10_X_10], X[NEGATIVE_10_X_10], Y[NEGATIVE_10_X_10]);
    }

    private void test(final long expected, final double x, final double y) {
        assertEquals(expected, new Grid(new Continuous2DEnvironment<>(), new MersenneTwister(), 0, 0, x, y, 1, 1, 0, 0).stream().count());
    }

}
