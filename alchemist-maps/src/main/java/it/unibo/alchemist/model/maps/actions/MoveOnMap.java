/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.actions;

import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.actions.AbstractConfigurableMoveNode;
import it.unibo.alchemist.model.maps.MapEnvironment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.RoutingService;
import it.unibo.alchemist.model.RoutingServiceOptions;
import it.unibo.alchemist.model.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy;
import it.unibo.alchemist.utils.Maps;

/**
 * @param <T> Concentration type
 * @param <O> {@link RoutingServiceOptions} type
 * @param <S> {@link RoutingService} type
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
        final RoutingStrategy<T, GeoPosition> routingStrategy,
        final SpeedSelectionStrategy<T, GeoPosition> speedSelectionStrategy,
        final TargetSelectionStrategy<T, GeoPosition> targetSelectionStrategy
    ) {
        super(environment, node, routingStrategy, targetSelectionStrategy, speedSelectionStrategy, true);
    }

    @Override
    public final MapEnvironment<T, O, S> getEnvironment() {
        return (MapEnvironment<T, O, S>) super.getEnvironment();
    }

    /**
     * Fails, can't be cloned.
     */
    @Override
    public MoveOnMap<T, O, S> cloneAction(final Node<T> node, final Reaction<T> reaction) {
        /*
         * Routing strategies can not be cloned at the moment.
         */
        return new MoveOnMap<>(
            getEnvironment(),
            node,
            getRoutingStrategy().cloneIfNeeded(node, reaction),
            getSpeedSelectionStrategy().cloneIfNeeded(node, reaction),
            getTargetSelectionStrategy().cloneIfNeeded(node, reaction)
        );
    }

    @Override
    protected final GeoPosition interpolatePositions(final GeoPosition current, final GeoPosition target, final double maxWalk) {
        return Maps.getDestinationLocation(current, target, maxWalk);
    }

}
