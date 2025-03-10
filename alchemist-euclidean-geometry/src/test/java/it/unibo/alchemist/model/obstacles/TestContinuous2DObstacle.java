/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.obstacles;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.SupportedIncarnations;
import it.unibo.alchemist.model.linkingrules.NoLinks;
import it.unibo.alchemist.model.nodes.GenericNode;
import it.unibo.alchemist.model.physics.environments.Continuous2DObstacles;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
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
            environment.next(new Euclidean2DPosition(0, 0), new Euclidean2DPosition(1, 1))
        );
        assertEquals(
            new Euclidean2DPosition(0, 0),
            environment.next(new Euclidean2DPosition(1, 1), new Euclidean2DPosition(0, 0)));
        assertEquals(
            new Euclidean2DPosition(nextAfter(1.0, 0.0), nextAfter(0.5, 0.0)),
            environment.next(
                new Euclidean2DPosition(0, 0),
                new Euclidean2DPosition(2, 1)
            )
        );

        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(0, 0));
        assertEquals(1, environment.getNodeCount());
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1, 1));
        assertEquals(1, environment.getNodeCount());
        // CHECKSTYLE: MagicNumber OFF
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1.5, 0.5));
        assertEquals(1, environment.getNodeCount());
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1, 5));
        assertEquals(1, environment.getNodeCount());
        environment.addNode(createIntNode(incarnation, environment), new Euclidean2DPosition(1, 2.999));
        assertEquals(2, environment.getNodeCount());
        assertEquals(2, environment.getObstaclesInRange(0d, 0d, 100d).size());
        assertEquals(1, environment.getObstaclesInRange(0d, 0d, 1d).size());
        assertEquals(R1021, environment.getObstaclesInRange(0d, 0d, 1d).get(0));
        assertEquals(1, environment.getObstaclesInRange(1d, 5d, 1d).size());
        assertEquals(R0527, environment.getObstaclesInRange(1d, 5d, 1d).get(0));
        // CHECKSTYLE: MagicNumber ON
        assertEquals(0, environment.getObstaclesInRange(0d, 0d, 0.5d).size());
    }

}
