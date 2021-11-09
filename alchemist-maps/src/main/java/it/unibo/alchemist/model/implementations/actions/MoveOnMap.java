/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.RoutingService;
import it.unibo.alchemist.model.interfaces.RoutingServiceOptions;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;
import it.unibo.alchemist.utils.MapUtils;

/**
 * @param <T> concentration type
 */
public class MoveOnMap<T, O extends RoutingServiceOptions<O>, S extends RoutingService<GeoPosition, O>>
    extends AbstractConfigurableMoveNode<T, GeoPosition> {

    private static final long serialVersionUID = 1L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param routingStrategy the {@link RoutingStrategy}
     * @param speedSelectionStrategy
     *            the {@link SpeedSelectionStrategy}
     * @param targetSelectionStrategy
     *            {@link TargetSelectionStrategy}
     */
    public MoveOnMap(
        final MapEnvironment<T, O, S> environment,
        final Node<T> node,
        final RoutingStrategy<GeoPosition> routingStrategy,
        final SpeedSelectionStrategy<GeoPosition> speedSelectionStrategy,
        final TargetSelectionStrategy<GeoPosition> targetSelectionStrategy
    ) {
        super(environment, node, routingStrategy, targetSelectionStrategy, speedSelectionStrategy, true);
    }

    @Override
    public final MapEnvironment<T> getEnvironment() {
        return (MapEnvironment<T>) super.getEnvironment();
    }

    /**
     * Fails, can't be cloned.
     */
    @Override
    public MoveOnMap<T> cloneAction(final Node<T> node, final Reaction<T> reaction) {
        /*
         * Routing strategies can not be cloned at the moment.
         */
        throw new UnsupportedOperationException("Routing strategies can not be cloned.");
    }

    @Override
    protected final GeoPosition interpolatePositions(final GeoPosition current, final GeoPosition target, final double maxWalk) {
        return MapUtils.getDestinationLocation(current, target, maxWalk);
    }

}
