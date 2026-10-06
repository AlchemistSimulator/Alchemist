/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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

import javax.annotation.Nonnull;
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

    /**
     * @param radius
     *            connection range
     */
    public ObstaclesBreakConnection(final Double radius) {
        super(radius);
    }

    @Nonnull
    @Override
    public Neighborhood<T> computeNeighborhood(@Nonnull final Node<T> center, @Nonnull final Environment<T, P> environment) {
        Neighborhood<T> normal = super.computeNeighborhood(center, environment);
        if (!normal.isEmpty() && environment instanceof final EnvironmentWithObstacles<?, T, P> environmentWithObstacles) {
            final P centerPosition = environment.getCurrentPosition(center);
            environmentWithObstacles.intersectsObstacle(
                environmentWithObstacles.getCurrentPosition(center),
                environmentWithObstacles.getCurrentPosition(center)
            );
            final Iterable<Node<T>> neighbors = StreamSupport.stream(normal.spliterator(), false)
                .filter(node ->
                    !environmentWithObstacles
                        .intersectsObstacle(centerPosition, environmentWithObstacles.getCurrentPosition(node))
                )
                .collect(Collectors.toList());
            normal = Neighborhoods.make(environmentWithObstacles, center, neighbors);
        }
        return normal;
    }

}
