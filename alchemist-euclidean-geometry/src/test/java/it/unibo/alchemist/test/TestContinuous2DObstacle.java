/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.model.api.SupportedIncarnations;
import it.unibo.alchemist.model.implementations.environments.Continuous2DObstacles;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.obstacles.RectObstacle2D;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Node;
import org.junit.jupiter.api.Test;

import static org.apache.commons.math3.util.FastMath.nextAfter;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
class TestContinuous2DObstacle {

    private static final RectObstacle2D<Euclidean2DPosition> R1021 = new RectObstacle2D<>(1, 0, 1, 1);
    private static final RectObstacle2D<Euclidean2DPosition> R0527 = new RectObstacle2D<>(0, 5, 2, -2);

    private Node<Integer> createIntNode(
        final Incarnation<Integer, Euclidean2DPosition> incarnation,
        final Environment<Integer, Euclidean2DPosition> environment
    ) {
        return new GenericNode<>(incarnation, environment) {
            @Override
            public Integer createT() {
                return 0;
            }
        };
    }

    /**
     * 
     */
    @Test
    void test() {
        final var incarnation = SupportedIncarnations.<Integer, Euclidean2DPosition>get("protelis").orElseThrow();
        final Continuous2DObstacles<Integer> environment = new Continuous2DObstacles<>(incarnation);
        environment.setLinkingRule(new NoLinks<>());
        environment.addObstacle(R1021);
        environment.addObstacle(R0527);

        assertEquals(
                new Euclidean2DPosition(nextAfter(1.0, 0.0), nextAfter(1.0, 0.0)),
                environment.next(0, 0, 1, 1)
        );
        assertEquals(new Euclidean2DPosition(0, 0), environment.next(1, 1, 0, 0));
        assertEquals(
                new Euclidean2DPosition(nextAfter(1.0, 0.0), nextAfter(0.5, 0.0)),
                environment.next(0, 0, 2, 1)
        );

        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(0, 0));
        assertEquals(environment.getNodeCount(), 1);
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1, 1));
        assertEquals(environment.getNodeCount(), 1);
        // CHECKSTYLE: MagicNumber OFF
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1.5, 0.5));
        assertEquals(environment.getNodeCount(), 1);
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1, 5));
        assertEquals(environment.getNodeCount(), 1);
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1, 2.999));
        assertEquals(environment.getNodeCount(), 2);
        assertEquals(environment.getObstaclesInRange(0d, 0d, 100d).size(), 2);
        assertEquals(environment.getObstaclesInRange(0d, 0d, 1d).size(), 1);
        assertEquals(environment.getObstaclesInRange(0d, 0d, 1d).get(0), R1021);
        assertEquals(environment.getObstaclesInRange(1d, 5d, 1d).size(), 1);
        assertEquals(environment.getObstaclesInRange(1d, 5d, 1d).get(0), R0527);
        // CHECKSTYLE: MagicNumber ON
        assertEquals(environment.getObstaclesInRange(0d, 0d, 0.5d).size(), 0);
    }

}
