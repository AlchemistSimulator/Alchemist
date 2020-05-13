/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.navigationstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EuclideanNavigationAction
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

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
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 */
open class DestinationReaching<T, N : Euclidean2DConvexShape, E>(
    action: EuclideanNavigationAction<T, N, E, ConvexPolygon, Euclidean2DPassage>,
    /**
     * Known destinations, can be empty.
     */
    private val knownDestinations: List<Euclidean2DPosition>,
    /**
     * Unknown destinations, defaults to an empty list.
     */
    private val unknownDestinations: List<Euclidean2DPosition> = emptyList()
) : GoalOrientedExploring<T, N, E> (
    action,
    /*
     * This may seem strange, but as stated above if we found a destination along the way (either known
     * or unknown), we want to approach it and leave the route we're following.
     */
    knownDestinations + unknownDestinations
) {

    private val knownDestinationReaching: KnownDestinationReaching<T, N, E>? =
        knownDestinations.takeIf { it.isNotEmpty() }?.let { KnownDestinationReaching(action, it) }

    override fun inNewRoom(newRoom: ConvexPolygon) = when (knownDestinationReaching) {
        null -> super.inNewRoom(newRoom)
        else -> reachUnknownDestination(newRoom, orElse = { knownDestinationReaching.inNewRoom(newRoom) })
    }
}
