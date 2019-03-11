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

/**
 * This {@link TraceDependantSpeed} uses the distance between coordinates for estimating the distance.
 * 
 * @param <T>
 */
public final class StraightLineTraceDependantSpeed<T> extends TraceDependantSpeed<T> {

    private static final long serialVersionUID = 539968590628143027L;

    /**
     * @param e
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     */
    public StraightLineTraceDependantSpeed(final MapEnvironment<T> e, final Node<T> n, final Reaction<T> r) {
        super(e, n, r);
    }

    @Override
    protected double computeDistance(final MapEnvironment<T> environment, final Node<T> curNode, final GeoPosition targetPosition) {
        return environment.getPosition(curNode).getDistanceTo(targetPosition);
    }

}
