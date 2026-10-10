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
import it.unibo.alchemist.model.maps.GPSTrace
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.movestrategies.routing.OnStreets
import it.unibo.alchemist.model.maps.movestrategies.speed.RoutingTraceDependantSpeed
import it.unibo.alchemist.model.maps.movestrategies.target.FollowTrace
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy

/**
 * A walker following a mandatory GPS trace on the streets of the map.
 *
 * @param T concentration type
 * @param O [RoutingServiceOptions] type
 * @param S [RoutingService] type
 */
class GPSTraceWalker<T, O : RoutingServiceOptions<O>, S : RoutingService<GeoPosition, O>> private constructor(
    environment: MapEnvironment<T, O, S>,
    reaction: NodeReaction<T>,
    routingStrategy: RoutingStrategy<T, GeoPosition>,
    speedSelectionStrategy: SpeedSelectionStrategy<T, GeoPosition>,
    targetSelectionStrategy: TargetSelectionStrategy<T, GeoPosition>,
    trace: GPSTrace,
) : MoveOnMapWithGPS<T, O, S>(
    environment,
    reaction,
    routingStrategy,
    speedSelectionStrategy,
    targetSelectionStrategy,
    trace,
) {
    private constructor(
        environment: MapEnvironment<T, O, S>,
        reaction: NodeReaction<T>,
        options: O,
        trace: GPSTrace,
    ) : this(
        environment,
        reaction,
        OnStreets(environment, options),
        RoutingTraceDependantSpeed(environment, reaction.host, reaction, options),
        FollowTrace(reaction),
        trace,
    )

    /**
     * Follows the trace loaded from [path], computing routes with [options].
     * Traces are distributed cyclically if [cycle] is true; their time is normalized by the strategy named
     * [normalizer], built with [normalizerArgs].
     */
    constructor(
        environment: MapEnvironment<T, O, S>,
        reaction: NodeReaction<T>,
        options: O,
        path: String,
        cycle: Boolean,
        normalizer: String,
        vararg normalizerArgs: Any?,
    ) : this(environment, reaction, options, traceFor(environment, path, cycle, normalizer, *normalizerArgs))

    /**
     * Follows the trace loaded from [path], computing routes with the default options of the routing service.
     * Traces are distributed cyclically if [cycle] is true; their time is normalized by the strategy named
     * [normalizer], built with [normalizerArgs].
     */
    constructor(
        environment: MapEnvironment<T, O, S>,
        reaction: NodeReaction<T>,
        path: String,
        cycle: Boolean,
        normalizer: String,
        vararg normalizerArgs: Any?,
    ) : this(
        environment,
        reaction,
        environment.routingService.defaultOptions,
        path,
        cycle,
        normalizer,
        *normalizerArgs,
    )

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): GPSTraceWalker<T, O, S> = GPSTraceWalker(
        environment,
        newReaction,
        routingStrategy.cloneIfNeeded(newReaction.host, newReaction),
        speedSelectionStrategy.cloneIfNeeded(newReaction.host, newReaction),
        targetSelectionStrategy.cloneIfNeeded(newReaction.host, newReaction),
        trace,
    )
}
