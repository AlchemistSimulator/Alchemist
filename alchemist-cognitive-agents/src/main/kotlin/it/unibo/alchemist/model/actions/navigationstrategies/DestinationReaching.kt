/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions.navigationstrategies

import it.unibo.alchemist.model.cognitiveagents.NavigationAction2D
import it.unibo.alchemist.model.cognitiveagents.NavigationStrategy
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A [NavigationStrategy] allowing to reach a (static) destination.
 * The client can specify a list of [knownDestinations] (see [Pursuing]) and [unknownDestinations] (see
 * [GoalOrientedExploring]).
 * The pedestrian will try to reach the closest known destination for which a valid path leading there is
 * known, but in case another destination is found along the way (either known or unknown), the latter will
 * be approached instead of the chosen known destination. To put it in another way, this behavior mixes
 * [KnownDestinationReaching] and [GoalOrientedExploring].
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the pedestrian's cognitive map.
 * @param R the type of edges of the pedestrian's cognitive map, representing the [R]elations between landmarks.
 */
open class DestinationReaching<T, L : Euclidean2DConvexShape, R>(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    /**
     * Known destinations, can be empty.
     */
    private val knownDestinations: List<Euclidean2DPosition>,
    /**
     * Unknown destinations, defaults to an empty list.
     */
    private val unknownDestinations: List<Euclidean2DPosition> = emptyList(),
) : GoalOrientedExploring<T, L, R> (
    action,
    /*
     * This may seem strange, but as stated above if we found a destination along the way (either known
     * or unknown), we want to approach it and leave the route we're following.
     */
    knownDestinations + unknownDestinations,
) {

    private val knownDestinationReaching: KnownDestinationReaching<T, L, R>? =
        knownDestinations.takeIf { it.isNotEmpty() }?.let { KnownDestinationReaching(action, it) }

    override fun inNewRoom(newRoom: ConvexPolygon) = when (knownDestinationReaching) {
        null -> super.inNewRoom(newRoom)
        else -> reachUnknownDestination(newRoom, orElse = { knownDestinationReaching.inNewRoom(newRoom) })
    }
}
