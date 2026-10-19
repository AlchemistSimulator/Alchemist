/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.actions

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.RoutingService
import it.unibo.alchemist.model.RoutingServiceOptions
import it.unibo.alchemist.model.actions.AbstractConfigurableMoveNode
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import it.unibo.alchemist.utils.Maps

/**
 * Moves the node on a map, combining a routing, a speed selection, and a target selection strategy.
 * Positions are absolute.
 *
 * @param T concentration type
 * @param O [RoutingServiceOptions] type
 * @param S [RoutingService] type
 */
open class MoveOnMap<T, O : RoutingServiceOptions<O>, S : RoutingService<GeoPosition, O>>(
    final override val environment: MapEnvironment<T, O, S>,
    reaction: NodeReaction<T>,
    routingStrategy: RoutingStrategy<T, GeoPosition>,
    speedSelectionStrategy: SpeedSelectionStrategy<T, GeoPosition>,
    targetSelectionStrategy: TargetSelectionStrategy<T, GeoPosition>,
) : AbstractConfigurableMoveNode<T, GeoPosition>(
    environment,
    reaction,
    routingStrategy,
    targetSelectionStrategy,
    speedSelectionStrategy,
    true,
) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): MoveOnMap<T, O, S> = MoveOnMap(
        environment,
        newReaction,
        routingStrategy.cloneIfNeeded(newReaction.host, newReaction),
        speedSelectionStrategy.cloneIfNeeded(newReaction.host, newReaction),
        targetSelectionStrategy.cloneIfNeeded(newReaction.host, newReaction),
    )

    final override fun interpolatePositions(current: GeoPosition, target: GeoPosition, maxWalk: Double): GeoPosition =
        Maps.getDestinationLocation(current, target, maxWalk)
}
