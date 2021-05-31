/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.model.implementations.environments.Continuous2DObstacles;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.IntNode;
import it.unibo.alchemist.model.implementations.obstacles.RectObstacle2D;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
class TestContinuous2DObstacle {

    private static final RectObstacle2D<Euclidean2DPosition> R1021 = new RectObstacle2D<>(1, 0, 1, 1);
    private static final RectObstacle2D<Euclidean2DPosition> R0527 = new RectObstacle2D<>(0, 5, 2, -2);

    /**
     * 
     */
    @Test
    void test() {
        final var incarnation = SupportedIncarnations.<Integer, Euclidean2DPosition>get("protelis").orElseThrow();
        final Continuous2DObstacles<Integer> env = new Continuous2DObstacles<>(incarnation);
        env.setLinkingRule(new NoLinks<>());
        env.addObstacle(R1021);
        env.addObstacle(R0527);

        assertEquals(new Euclidean2DPosition(FastMath.nextAfter(1.0, 0.0), FastMath.nextAfter(1.0, 0.0)), env.next(0, 0, 1, 1));
        assertEquals(new Euclidean2DPosition(0, 0), env.next(1, 1, 0, 0));
        assertEquals(new Euclidean2DPosition(FastMath.nextAfter(1.0, 0.0), FastMath.nextAfter(0.5, 0.0)), env.next(0, 0, 2, 1));

        env.addNode(new IntNode(env), new Euclidean2DPosition(0, 0));
        assertEquals(env.getNodeCount(), 1);
        env.addNode(new IntNode(env), new Euclidean2DPosition(1, 1));
        assertEquals(env.getNodeCount(), 1);
        // CHECKSTYLE: MagicNumber OFF
        env.addNode(new IntNode(env), new Euclidean2DPosition(1.5, 0.5));
        assertEquals(env.getNodeCount(), 1);
        env.addNode(new IntNode(env), new Euclidean2DPosition(1, 5));
        assertEquals(env.getNodeCount(), 1);
        env.addNode(new IntNode(env), new Euclidean2DPosition(1, 2.999));
        assertEquals(env.getNodeCount(), 2);
        assertEquals(env.getObstaclesInRange(0d, 0d, 100d).size(), 2);
        assertEquals(env.getObstaclesInRange(0d, 0d, 1d).size(), 1);
        assertEquals(env.getObstaclesInRange(0d, 0d, 1d).get(0), R1021);
        assertEquals(env.getObstaclesInRange(1d, 5d, 1d).size(), 1);
        assertEquals(env.getObstaclesInRange(1d, 5d, 1d).get(0), R0527);
        // CHECKSTYLE: MagicNumber ON
        assertEquals(env.getObstaclesInRange(0d, 0d, 0.5d).size(), 0);
    }

}
