/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.loader.displacements.Grid;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class TestGrid {

    private static Continuous2DEnvironment env() {
        return new Continuous2DEnvironment<>();
    }

    private static MersenneTwister rand() {
        return new MersenneTwister();
    }

    // CHECKSTYLE: MagicNumber OFF
    /**
     * 
     */
    @Test
    public void testVerticalLine() {
        test(9, 1, 9.9);
        assertEquals(10L, new Grid(env(), rand(), 0, 0, 1, 10, 1, 1, 0, 0).stream().count());
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
    public void testbug73() {
        assertEquals(20L * 20, new Grid(env(), rand(), 0, 0, 20, 20, 1, 1, 0.8, 0.8).stream().distinct().count());
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
