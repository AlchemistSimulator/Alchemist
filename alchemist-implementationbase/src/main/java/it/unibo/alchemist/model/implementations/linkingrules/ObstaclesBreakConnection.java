/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.geometry.Vector;

/**
 * Similar to {@link ConnectWithinDistance}, but if the environment has obstacles,
 * the links are removed.
 *
 * @param <P> position type
 * @param <T> concentration type
 */
public final class ObstaclesBreakConnection<T, P extends Position<P> & Vector<P>> extends ConnectWithinDistance<T, P> {

    private static final long serialVersionUID = -3279202906910960340L;

    /**
     * @param radius
     *            connection range
     */
    public ObstaclesBreakConnection(final Double radius) {
        super(radius);
    }

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> env) {
        Neighborhood<T> normal = super.computeNeighborhood(center, env);
        if (!normal.isEmpty() && env instanceof EnvironmentWithObstacles) {
            final P cp = env.getPosition(center);
            final EnvironmentWithObstacles<?, T, P> environment = (EnvironmentWithObstacles<?, T, P>) env;
            environment.intersectsObstacle(environment.getPosition(center), environment.getPosition(center));
            normal = Neighborhoods.make(env, center, StreamSupport.stream(normal.spliterator(), false)
                    .filter(node -> !environment.intersectsObstacle(cp, environment.getPosition(node)))
                    .collect(Collectors.toList()));
        }
        return normal;
    }

}
