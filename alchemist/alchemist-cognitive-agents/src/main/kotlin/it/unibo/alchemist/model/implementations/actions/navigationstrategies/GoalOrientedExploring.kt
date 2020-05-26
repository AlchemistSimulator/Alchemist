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
 * A [NavigationStrategy] allowing to explore the environment looking for something specific whose position
 * is unknown.
 * The client can specify a list of [unknownDestinations]: these can be recognized once they're in sight,
 * but the pedestrian doesn't know their position until that moment (think e.g. of exits in an evacuation
 * scenario). More specifically, unknown destinations can be detected if located in a room adjacent to the
 * room the pedestrian is into. Once a destination is detected, the pedestrian will reach it and stop.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 */
open class GoalOrientedExploring<T, N : Euclidean2DConvexShape, E>(
    action: EuclideanNavigationAction<T, N, E, ConvexPolygon, Euclidean2DPassage>,
    private val unknownDestinations: List<Euclidean2DPosition>
) : Exploring<T, N, E>(action) {

    override fun inNewRoom(newRoom: ConvexPolygon) =
        reachUnknownDestination(newRoom, orElse = { super.inNewRoom(newRoom) })

    /**
     * If one or more unknown destinations are inside [newRoom] (= the room the pedestrian is into), the closest
     * one is approached. Otherwise, if one or more destinations are in a room adjacent to the current one, the
     * related doors are weighted using [weightExit] and the one with minimum weight is crossed. [orElse] is
     * executed otherwise.
     */
    protected open fun reachUnknownDestination(newRoom: ConvexPolygon, orElse: () -> Unit) = with(action) {
        unknownDestinations
            .filter { newRoom.contains(it) }
            .minBy { it.distanceTo(pedestrianPosition) }
            ?.let { moveToFinal(it) }
            ?: (doorsInSight()
                .filter { it.leadsToUnknownDestination() }
                .minBy { weightExit(it) }
                ?.let { crossDoor(it) }
                ?: orElse())
    }

    protected open fun Euclidean2DPassage.leadsToUnknownDestination(): Boolean =
        unknownDestinations.any { head.contains(it) }

    /**
     * Assigns a weight to a door (= passage) leading to an unknown destination (e.g. an exit).
     * By default, the exit's distance and its congestion are considered.
     */
    protected open fun weightExit(door: Euclidean2DPassage): Double = with(door) {
        distanceToPedestrian() * congestionFactor(head)
    }
}
