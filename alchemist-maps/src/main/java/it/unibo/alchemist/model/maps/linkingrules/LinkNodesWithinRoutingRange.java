/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.linkingrules;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.maps.MapEnvironment;
import it.unibo.alchemist.model.networks.ObservingRangeBasedNetworkModel;
import it.unibo.alchemist.model.observation.Observable;

import javax.annotation.Nonnull;

/**
 * Links the nodes whose route is shorter than a range; nodes outside a {@link MapEnvironment} are never linked.
 *
 * @param <T> concentration type
 */
public final class LinkNodesWithinRoutingRange<T> extends ObservingRangeBasedNetworkModel<T, GeoPosition, GeoPosition> {

    /**
     * @param environment the environment whose nodes this model links
     * @param r range
     */
    public LinkNodesWithinRoutingRange(@Nonnull final Environment<T, GeoPosition> environment, final double r) {
        super(environment, r);
    }

    /**
     * Routes, computed between the current positions of the nodes, exist only in a {@link MapEnvironment}.
     */
    @Override
    protected boolean isLinked(
        @Nonnull final Node<T> center,
        @Nonnull final GeoPosition centerInput,
        @Nonnull final Node<T> neighbor,
        @Nonnull final GeoPosition neighborInput
    ) {
        return getEnvironment() instanceof final MapEnvironment<T, ?, ?> map
            && map.computeRoute(center, neighbor).length() < getRange();
    }

    /**
     * Routes depend on the positions of both nodes.
     */
    @Nonnull
    @Override
    protected Observable<GeoPosition> observeInput(@Nonnull final Node<T> node) {
        return getEnvironment().positionOf(node);
    }
}
