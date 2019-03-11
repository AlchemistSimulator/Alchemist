/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.movestrategies.speed;

import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Vehicle;

/**
 * This {@link TraceDependantSpeed} strategy computes the remaining distance by
 * relying on maps data for a selected {@link Vehicle}.
 * 
 * @param <T>
 */
public final class RoutingTraceDependantSpeed<T> extends TraceDependantSpeed<T> {

    private static final long serialVersionUID = -2195494825891818353L;
    private final Vehicle v;

    /**
     * @param e
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     * @param vehicle
     *            the vehicle
     */
    public RoutingTraceDependantSpeed(final MapEnvironment<T> e, final Node<T> n, final Reaction<T> r, final Vehicle vehicle) {
        super(e, n, r);
        v = vehicle;
    }

    @Override
    protected double computeDistance(final MapEnvironment<T> environment, final Node<T> curNode, final GeoPosition targetPosition) {
        return environment.computeRoute(curNode, targetPosition, v).length();
    }

}
