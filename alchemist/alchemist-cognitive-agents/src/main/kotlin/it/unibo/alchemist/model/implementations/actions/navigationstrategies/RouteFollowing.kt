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
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.EuclideanNavigationAction
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

/**
 * A [NavigationStrategy] allowing to follow a given [route].
 * The [route] consists of a list of positions (= waypoints) that may or may not be in sight of each
 * other (i.e. the path leading from a waypoint to the next one may or may not be representable
 * as a single segment), for this reason [Pursuing] behavior is used to reach each waypoint.
 * In this context, a waypoint is considered reached when it's inside the current room (not when the
 * pedestrian reach that exact position), apart from the last waypoint which is actually approached.
 * Cuts to the route are allowed (i.e. if the pedestrian finds a waypoint which is farther than the
 * expected next one, he/she skips all the waypoints in between).
 */
open class RouteFollowing<T, N : Euclidean2DConvexShape, E> constructor(
    action: EuclideanNavigationAction<T, N, E, ConvexPolygon, Euclidean2DPassage>,
    /**
     * The route to follow.
     */
    protected val route: List<Euclidean2DPosition>
) : DynamicPursuing<T, N, E>(action, action.environment.origin().takeIf { route.isEmpty() } ?: route[0]) {

    private var indexOfNextWaypoint: Int = 0

    override fun inNewRoom(newRoom: ConvexPolygon) = when {
        route.isEmpty() -> action.stop()
        else -> {
            if (waypointReached(newRoom)) {
                setDestination(route[indexOfNextWaypoint])
            }
            super.inNewRoom(newRoom)
        }
    }

    /**
     * Checks if a new waypoint has been reached and updates [indexOfNextWaypoint] accordingly.
     */
    protected fun waypointReached(currentRoom: ConvexPolygon): Boolean = when {
        indexOfNextWaypoint >= route.size - 1 -> false
        else -> route.indexOfLast { isReached(it, currentRoom) }.let { indexOfFartherWaypointReached ->
            /*
             * [indexOfNextWaypoint] is updated only if a farther waypoint is reached.
             */
            if (indexOfFartherWaypointReached >= indexOfNextWaypoint) {
                indexOfNextWaypoint = (indexOfFartherWaypointReached + 1).coerceAtMost(route.size - 1)
                true
            } else {
                false
            }
        }
    }

    /**
     * Checks if the given [waypoint] is reached (as stated above, if it's inside [currentRoom]).
     */
    protected open fun isReached(waypoint: Euclidean2DPosition, currentRoom: ConvexPolygon): Boolean =
        currentRoom.contains(waypoint)
}
