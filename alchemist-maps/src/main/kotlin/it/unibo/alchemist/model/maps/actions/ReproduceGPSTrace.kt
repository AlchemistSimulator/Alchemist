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
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.movestrategies.routing.IgnoreStreets
import it.unibo.alchemist.model.maps.movestrategies.speed.StraightLineTraceDependantSpeed
import it.unibo.alchemist.model.maps.movestrategies.target.FollowTrace
import it.unibo.alchemist.model.movestrategies.speed.ConstantSpeed

/**
 * Reproduces a GPS trace, moving along straight lines regardless of the streets.
 * The rate of the owning reaction determines the distance walked at each step.
 *
 * @param T concentration type
 * @param O [RoutingServiceOptions] type
 * @param S [RoutingService] type
 */
open class ReproduceGPSTrace<T, O : RoutingServiceOptions<O>, S : RoutingService<GeoPosition, O>> :
    MoveOnMapWithGPS<T, O, S> {
    /**
     * Follows the trace loaded from [path] at the speed recorded in the trace.
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
    ) : super(
        environment,
        reaction,
        IgnoreStreets(),
        StraightLineTraceDependantSpeed(environment, reaction.host, reaction),
        FollowTrace(reaction),
        path,
        cycle,
        normalizer,
        *normalizerArgs,
    )

    /**
     * Follows the trace loaded from [path] at the average [speed].
     * Traces are distributed cyclically if [cycle] is true; their time is normalized by the strategy named
     * [normalizer], built with [normalizerArgs].
     */
    constructor(
        environment: MapEnvironment<T, O, S>,
        reaction: NodeReaction<T>,
        speed: Double,
        path: String,
        cycle: Boolean,
        normalizer: String,
        vararg normalizerArgs: Any?,
    ) : super(
        environment,
        reaction,
        IgnoreStreets(),
        ConstantSpeed(reaction, speed),
        FollowTrace(reaction),
        path,
        cycle,
        normalizer,
        *normalizerArgs,
    )
}
