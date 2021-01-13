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
 * @param <T> concentration type
 */
public final class StraightLineTraceDependantSpeed<T> extends TraceDependantSpeed<T> {

    private static final long serialVersionUID = 1L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     */
    public StraightLineTraceDependantSpeed(
            final MapEnvironment<T> environment,
            final Node<T> node,
            final Reaction<T> reaction
    ) {
        super(environment, node, reaction);
    }

    @Override
    protected double computeDistance(
            final MapEnvironment<T> environment,
            final Node<T> currentNode,
            final GeoPosition targetPosition
    ) {
        return environment.getPosition(currentNode).distanceTo(targetPosition);
    }

}
