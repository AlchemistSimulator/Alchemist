/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.movestrategies.routing;

import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Route;
import it.unibo.alchemist.model.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.routes.PolygonalChain;

import java.io.Serial;

/**
 * This strategy ignores any information about the map, and connects the
 * starting and ending point with a straight line using
 * {@link PolygonalChain}.
 *
 * @param <T> Concentration type
 * @param <P> position type
 */
public final class IgnoreStreets<T, P extends Position<P>> implements RoutingStrategy<T, P> {

    @Serial
    private static final long serialVersionUID = 2678088737744440021L;

    @Override
    public Route<P> computeRoute(final P currentPos, final P finalPos) {
        return new PolygonalChain<>(currentPos, finalPos);
    }

}
