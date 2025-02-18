/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.linkingrules;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.EnvironmentWithObstacles;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.geometry.Vector;
import it.unibo.alchemist.model.neighborhoods.Neighborhoods;

import java.io.Serial;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Similar to {@link ConnectWithinDistance}, but if the environment has obstacles,
 * the links are removed.
 *
 * @param <P> position type
 * @param <T> concentration type
 */
public final class ObstaclesBreakConnection<T, P extends Position<P> & Vector<P>> extends ConnectWithinDistance<T, P> {

    @Serial
    private static final long serialVersionUID = -3279202906910960340L;

    /**
     * @param radius
     *            connection range
     */
    public ObstaclesBreakConnection(final Double radius) {
        super(radius);
    }

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> environment) {
        Neighborhood<T> normal = super.computeNeighborhood(center, environment);
        if (!normal.isEmpty() && environment instanceof final EnvironmentWithObstacles<?, T, P> environmentWithObstacles) {
            final P centerPosition = environment.getPosition(center);
            environmentWithObstacles.intersectsObstacle(
                environmentWithObstacles.getPosition(center),
                environmentWithObstacles.getPosition(center)
            );
            final Iterable<Node<T>> neighbors = StreamSupport.stream(normal.spliterator(), false)
                .filter(node ->
                    !environmentWithObstacles
                        .intersectsObstacle(centerPosition, environmentWithObstacles.getPosition(node))
                )
                .collect(Collectors.toList());
            normal = Neighborhoods.make(environmentWithObstacles, center, neighbors);
        }
        return normal;
    }

}
