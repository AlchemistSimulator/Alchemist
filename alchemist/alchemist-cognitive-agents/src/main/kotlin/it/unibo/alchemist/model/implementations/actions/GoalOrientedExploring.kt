/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingAction
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

/**
 * An [OrientingAction] allowing to explore the environment looking for something specific whose position
 * is unknown.
 * The client can specify a list of [unknownDestinations]: these can be recognized once they're in sight,
 * but the pedestrian doesn't know their position until that moment (think e.g. of exits in an evacuation
 * scenario). More specifically, unknown destinations can be sensed if they're located in a room adjacent
 * to the room the pedestrian is into.
 */
open class GoalOrientedExploring<T, N : Euclidean2DConvexShape, E>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    private val unknownDestinations: List<Euclidean2DPosition>
) : Exploring<T, N, E>(environment, reaction, pedestrian) {

    constructor(
        environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
        reaction: Reaction<T>,
        pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
        vararg unknownDestinations: Number
    ) : this(environment, reaction, pedestrian, unknownDestinations.toPositions())

    override fun explore(): Unit = reachUnknownDestination(orElse = { super.explore() })

    /**
     * If one or more [unknownDestinations] can be sensed (as defined above), they are weighted using
     * [weightExit] and the one with minimum weight is crossed. [orElse] is executed if no unknown
     * destination is in sight.
     */
    protected open fun reachUnknownDestination(orElse: () -> Unit) {
        unknownDestinations
            .filter { currentRoomOrFail().customContains(it) }
            .minBy { it.distanceTo(cachedCurrentPosition) }
            ?.let { moveToFinal(it) }
            ?: (doorsInSight()
                .filter { it.leadsToUnknownDestination() }
                .minBy { weightExit(it) }
                ?.let { crossDoor(it) }
                ?: orElse())
    }

    /**
     * Expose base [explore] behavior to subclasses (only for performance).
     */
    protected fun baseExplore(): Unit = super.explore()

    protected open fun Euclidean2DPassage.leadsToUnknownDestination(): Boolean =
        unknownDestinations.any { head.customContains(it) }

    /**
     * Assigns a weight to a passage leading to an unknown destination (e.g. an exit).
     * By default, the exit's distance and its congestion are considered.
     */
    protected open fun weightExit(passage: Euclidean2DPassage): Double = with(passage) {
        distanceToPedestrian() * congestionFactor(head)
    }
}
