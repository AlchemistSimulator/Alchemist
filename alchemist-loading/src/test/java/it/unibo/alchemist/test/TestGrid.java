/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.loader.deployments.Grid;
import it.unibo.alchemist.model.SupportedIncarnations;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.environments.Continuous2DEnvironment;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class TestGrid {

    private static Continuous2DEnvironment<?> environment() {
        return new Continuous2DEnvironment<>(
            SupportedIncarnations.<Object, Euclidean2DPosition>get("protelis").orElseThrow()
        );
    }

    private static MersenneTwister randomGenerator() {
        return new MersenneTwister();
    }

    // CHECKSTYLE: MagicNumber OFF
    /**
     * 
     */
    @Test
    void testVerticalLine() {
        test(10, 1, 9.9);
        assertEquals(10L, new Grid(environment(), randomGenerator(), 0, 0, 1, 10, 1, 1, 0, 0).stream().count());
    }

    /**
     * 
     */
    @Test
    void testHorizontalLine() {
        test(10, 10, 1);
    }

    /**
     * 
     */
    @Test
    void testEmpty() {
        test(0, 0, 0);
    }

    /**
     * 
     */
    @Test
    void test1x1() {
        test(1, 1, 1);
    }

    /**
     * 
     */
    @Test
    void test10x10() {
        test(100, 10, 10);
    }

    /**
     *
     */
    @Test
    void testbug73() {
        assertEquals(
                20L * 20,
                new Grid(environment(), randomGenerator(), 0, 0, 20, 20, 1, 1, 0.8, 0.8)
                    .stream().distinct().count()
        );
    }

    /**
     * 
     */
    @Test
    void test10x10negative() {
        test(100, -10, -10);
    }

    private void test(final long expected, final double x, final double y) {
        assertEquals(
                expected,
                new Grid(
                        environment(),
                        new MersenneTwister(),
                        0,
                        0,
                        x,
                        y,
                        1,
                        1,
                        0,
                        0
                ).stream().count()
        );
    }

}
