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
import it.unibo.alchemist.model.maps.MapEnvironment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.RoutingService;
import it.unibo.alchemist.model.RoutingServiceOptions;

/**
 * This {@link TraceDependantSpeed} strategy computes the remaining distance by
 * relying on maps data for the selected {@link RoutingServiceOptions}.
 *
 * @param <T> Concentration type
 * @param <O> {@link RoutingServiceOptions} type
 * @param <S> {@link RoutingService} type
 */
public final class RoutingTraceDependantSpeed<T, O extends RoutingServiceOptions<O>, S extends RoutingService<GeoPosition, O>>
    extends TraceDependantSpeed<T, O, S> {

    private static final long serialVersionUID = -2195494825891818353L;
    private final O options;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param options
     *            the options
     */
    public RoutingTraceDependantSpeed(
            final MapEnvironment<T, O, S> environment,
            final Node<T> node,
            final Reaction<T> reaction,
            final O options
    ) {
        super(environment, node, reaction);
        this.options = options;
    }

    @Override
    protected double computeDistance(
            final MapEnvironment<T, O, S> environment,
            final Node<T> currentNode,
            final GeoPosition targetPosition
    ) {
        return environment.computeRoute(currentNode, targetPosition, options).length();
    }

    @Override
    public RoutingTraceDependantSpeed<T, O, S> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return new RoutingTraceDependantSpeed<>(getEnvironment(), destination, reaction, options);
    }
}
