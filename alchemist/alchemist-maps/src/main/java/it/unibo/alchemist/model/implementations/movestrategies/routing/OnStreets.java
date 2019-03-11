/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.routing;

import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.Vehicle;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;

/**
 * This strategy computes a route along streets allowed for a selected
 * {@link Vehicle} connecting the starting and ending point.
 * 
 * @param <T>
 */
public class OnStreets<T> implements RoutingStrategy<GeoPosition> {

    private static final long serialVersionUID = 9041363003794088201L;
    private final MapEnvironment<T> env;
    private final Vehicle vehicle;

    /**
     * @param environment
     *            the environment
     * @param v
     *            the {@link Vehicle}
     */
    public OnStreets(final MapEnvironment<T> environment, final Vehicle v) {
        env = environment;
        vehicle = v;
    }

    @Override
    public Route<GeoPosition> computeRoute(final GeoPosition currentPos, final GeoPosition finalPos) {
        return env.computeRoute(currentPos, finalPos, vehicle);
    }

}
