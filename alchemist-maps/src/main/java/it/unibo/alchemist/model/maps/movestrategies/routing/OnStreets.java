/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.movestrategies.routing;

import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Route;
import it.unibo.alchemist.model.RoutingService;
import it.unibo.alchemist.model.RoutingServiceOptions;
import it.unibo.alchemist.model.maps.MapEnvironment;
import it.unibo.alchemist.model.movestrategies.RoutingStrategy;

import javax.annotation.Nonnull;

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

    @Nonnull
    @Override
    public Route<GeoPosition> computeRoute(@Nonnull final GeoPosition currentPos, @Nonnull final GeoPosition finalPos) {
        return environment.computeRoute(currentPos, finalPos, options);
    }

}
