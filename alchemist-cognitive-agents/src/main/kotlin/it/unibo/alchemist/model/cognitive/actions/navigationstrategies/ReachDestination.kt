/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions.navigationstrategies

import it.unibo.alchemist.model.cognitive.NavigationStrategy
import it.unibo.alchemist.model.cognitive.actions.NavigationAction2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A [NavigationStrategy] allowing to reach a (static) destination.
 * The client can specify a list of [knownDestinations] (see [Pursue]) and [unknownDestinations] (see
 * [GoalOrientedExploration]).
 * The pedestrian will try to reach the closest known destination for which a valid path leading there is
 * known, but in case another destination is found along the way (either known or unknown), the latter will
 * be approached instead of the chosen known destination. To put it in another way, this behavior mixes
 * [ReachKnownDestination] and [GoalOrientedExploration].
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the pedestrian's cognitive map.
 * @param R the type of edges of the pedestrian's cognitive map, representing the [R]elations between landmarks.
 */
open class ReachDestination<T, L : Euclidean2DConvexShape, R>(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    /**
     * Known destinations, can be empty.
     */
    private val knownDestinations: List<Euclidean2DPosition>,
    /**
     * Unknown destinations, defaults to an empty list.
     */
    private val unknownDestinations: List<Euclidean2DPosition> = emptyList(),
) : GoalOrientedExploration<T, L, R> (
    action,
    /*
     * This may seem strange, but as stated above if we found a destination along the way (either known
     * or unknown), we want to approach it and leave the route we're following.
     */
    knownDestinations + unknownDestinations,
) {

    private val reachKnownDestination: ReachKnownDestination<T, L, R>? =
        knownDestinations.takeIf { it.isNotEmpty() }?.let { ReachKnownDestination(action, it) }

    override fun inNewRoom(newRoom: ConvexPolygon) = when (reachKnownDestination) {
        null -> super.inNewRoom(newRoom)
        else -> reachUnknownDestination(newRoom, orElse = { reachKnownDestination.inNewRoom(newRoom) })
    }
}
