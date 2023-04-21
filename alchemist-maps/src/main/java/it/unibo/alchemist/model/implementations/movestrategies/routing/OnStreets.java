/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.routing;

import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.RoutingService;
import it.unibo.alchemist.model.interfaces.RoutingServiceOptions;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;

/**
 * This strategy computes a route along streets allowed for a selected
 * {@link RoutingServiceOptions} connecting the starting and ending point.
 *
 * @param <T> Concentration type
 * @param <O> {@link RoutingServiceOptions} type
 * @param <S> {@link RoutingService} type
 */
public final class OnStreets<T, O extends RoutingServiceOptions<O>, S extends RoutingService<GeoPosition, O>>
    implements RoutingStrategy<T, GeoPosition> {

    private static final long serialVersionUID = 9041363003794088201L;
    private final MapEnvironment<T, O, S> environment;
    private final O options;

    /**
     * @param environment
     *            the environment
     * @param options
     *            the {@link RoutingServiceOptions}
     */
    public OnStreets(final MapEnvironment<T, O, S> environment, final O options) {
        this.environment = environment;
        this.options = options;
    }

    @Override
    public Route<GeoPosition> computeRoute(final GeoPosition currentPos, final GeoPosition finalPos) {
        return environment.computeRoute(currentPos, finalPos, options);
    }

}
