/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.navigation

import it.unibo.alchemist.model.cognitive.actions.NavigationAction2D
import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Navigation strategy that aims to reach a static destination.
 *
 * The strategy mixes known-target routing ([ReachKnownDestination]) and exploratory
 * goal-oriented behavior ([GoalOrientedExploration]). The pedestrian prefers the
 * closest known destination for which a path exists, but will divert to any
 * destination encountered along the way (either known or unknown).
 *
 * @param T the concentration type.
 * @param L the landmark shape type used by the pedestrian's cognitive map.
 * @param R the relation/edge type used by the pedestrian's cognitive map.
 * @param action the navigation action driving this strategy.
 * @param knownDestinations a list of known static destinations (may be empty).
 * @param unknownDestinations optional unknown destinations to consider while exploring.
 */
open class ReachDestination<T, L : Euclidean2DConvexShape, R>(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    /** Known destinations, can be empty. */
    private val knownDestinations: List<Euclidean2DPosition>,
    /** Unknown destinations, defaults to an empty list. */
    private val unknownDestinations: List<Euclidean2DPosition> = emptyList(),
) : GoalOrientedExploration<T, L, R>(
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
