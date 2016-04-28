/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Similar to {@link EuclideanDistance}, but if the environment has obstacles,
 * the links are removed.
 * 
 * @param <T>
 */
public class ObstaclesBreakConnection<T> extends EuclideanDistance<T> {

    private static final long serialVersionUID = -3279202906910960340L;

    /**
     * @param radius
     *            connection range
     */
    public ObstaclesBreakConnection(final Double radius) {
        super(radius);
    }

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T> env) {
        final Neighborhood<T> normal = super.computeNeighborhood(center, env);
        if (!normal.isEmpty() && env instanceof Environment2DWithObstacles) {
            final Position cp = env.getPosition(center);
            @SuppressWarnings("unchecked")
            final Environment2DWithObstacles<?, T> environment = (Environment2DWithObstacles<?, T>) env;
            for (int i = 0; i < normal.size(); i++) {
                final Node<T> node = normal.getNeighborByNumber(i);
                final Position np = environment.getPosition(node);
                if (environment.intersectsObstacle(cp, np)) {
                    normal.removeNeighbor(node);
                    i--;
                }
            }
        }
        return normal;
    }

}
