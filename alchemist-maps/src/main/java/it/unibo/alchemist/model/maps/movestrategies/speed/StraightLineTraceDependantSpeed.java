/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.movestrategies.speed;

import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.RoutingService;
import it.unibo.alchemist.model.RoutingServiceOptions;
import it.unibo.alchemist.model.maps.MapEnvironment;

import java.io.Serial;

/**
 * This {@link AbstractTraceDependantSpeed} uses the distance between coordinates for estimating the distance.
 *
 * @param <T> Concentration type
 * @param <O> {@link RoutingServiceOptions} type
 * @param <S> {@link RoutingService} type
 */
public final class StraightLineTraceDependantSpeed<
        T,
        O extends RoutingServiceOptions<O>,
        S extends RoutingService<GeoPosition, O>
    >
    extends AbstractTraceDependantSpeed<T, O, S> {

    @Serial
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
        final MapEnvironment<T, O, S> environment,
        final Node<T> node,
        final Reaction<T> reaction
    ) {
        super(environment, node, reaction);
    }

    @Override
    protected double computeDistance(
        final MapEnvironment<T, O, S> environment,
        final Node<T> currentNode,
        final GeoPosition targetPosition
    ) {
        return environment.getPosition(currentNode).distanceTo(targetPosition);
    }

    @Override
    public StraightLineTraceDependantSpeed<T, O, S> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return new StraightLineTraceDependantSpeed<>(getEnvironment(), destination, reaction);
    }
}
