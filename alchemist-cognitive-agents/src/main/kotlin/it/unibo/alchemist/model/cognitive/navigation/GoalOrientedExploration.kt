/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
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
 * Navigation strategy that explores the environment looking for unknown destination points.
 * Unknown destinations are not known a priori but can be detected when they are inside a room
 * adjacent to the node's current room. When detected, the node approaches the destination and stops.
 *
 * @param T the concentration type.
 * @param L the landmark shape type used by the node's cognitive map.
 * @param R the relation/edge type used by the node's cognitive map.
 * @param action the navigation action driving this strategy.
 * @param unknownDestinations list of static destinations that can be detected when in sight.
 */
open class GoalOrientedExploration<T, L : Euclidean2DConvexShape, R>(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    private val unknownDestinations: List<Euclidean2DPosition>,
) : Explore<T, L, R>(action) {
    override fun inNewRoom(newRoom: ConvexPolygon) =
        reachUnknownDestination(newRoom, orElse = { super.inNewRoom(newRoom) })

    /**
     * If any unknown destination is inside [newRoom], approach the closest one. Otherwise, if any unknown
     * destination lies in an adjacent room, pick the best door using [weightExit] and cross it. If none applies,
     * execute [orElse].
     */
    protected open fun reachUnknownDestination(newRoom: ConvexPolygon, orElse: () -> Unit) = with(action) {
        unknownDestinations
            .filter { newRoom.contains(it) }
            .minByOrNull { it.distanceTo(pedestrianPosition) }
            ?.let { moveToFinal(it) }
            ?: doorsInSight()
                .filter { it.leadsToUnknownDestination() }
                .minByOrNull { weightExit(it) }
                ?.let { crossDoor(it) }
            ?: orElse()
    }

    protected open fun Euclidean2DPassage.leadsToUnknownDestination(): Boolean = unknownDestinations.any {
        head.contains(it)
    }

    /**
     * Assigns a weight to a door leading to an unknown destination. By default it considers
     * the exit's distance and congestion.
     */
    protected open fun weightExit(door: Euclidean2DPassage): Double = with(door) {
        distanceToPedestrian() * congestionFactor(head)
    }
}
