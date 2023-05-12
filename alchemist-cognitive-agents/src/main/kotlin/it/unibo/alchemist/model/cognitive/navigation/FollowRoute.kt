/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.navigation

import it.unibo.alchemist.model.cognitive.NavigationStrategy
import it.unibo.alchemist.model.cognitive.actions.NavigationAction2D
import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A [NavigationStrategy] allowing to follow a given route.
 * The route consists of a list of positions (= waypoints) that may or may not be in sight of each
 * other (i.e. the path leading from a waypoint to the next one may or may not be representable
 * as a single segment), for this reason [Pursue] behavior is used to reach each waypoint.
 * In this context, a waypoint is considered reached when it's inside the current room (not when the
 * node reach that exact position), apart from the last waypoint which is actually approached.
 * Cuts to the route are allowed (i.e. if the node finds a waypoint which is farther than the
 * expected next one, he/she skips all the waypoints in between).
 *
 * @param route the route to follow.
 * @param T the concentration type.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 */
open class FollowRoute<T, L : Euclidean2DConvexShape, R> constructor(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    /**
     * The route to follow.
     */
    protected open val route: List<Euclidean2DPosition>,
) : DynamicallyPursue<T, L, R>(action, action.environment.origin.takeIf { route.isEmpty() } ?: route[0]) {

    private var indexOfNextWaypoint: Int = 0

    /**
     * When in an unexpected room the node gets back to [previousRoom] to continue following the
     * route correctly.
     */
    override fun inUnexpectedNewRoom(
        previousRoom: ConvexPolygon,
        expectedNewRoom: ConvexPolygon,
        actualNewRoom: ConvexPolygon,
    ) = with(action) {
        doorsInSight()
            .filter { it.head == previousRoom }
            .minByOrNull { it.distanceToPedestrian() }
            ?.let { crossDoor(it) }
            ?: inNewRoom(actualNewRoom)
    }

    override fun inNewRoom(newRoom: ConvexPolygon) {
        if (route.isEmpty()) {
            action.stop()
        } else {
            if (waypointReached(newRoom)) {
                setDestination(route[indexOfNextWaypoint])
            }
            super.inNewRoom(newRoom)
        }
    }

    /**
     * Checks if a new waypoint has been reached and updates [indexOfNextWaypoint] accordingly.
     */
    protected fun waypointReached(currentRoom: ConvexPolygon): Boolean {
        if (indexOfNextWaypoint >= route.size - 1) {
            return false
        }
        val indexOfFartherWaypointReached = route.indexOfLast { isReached(it, currentRoom) }
        val newWaypointReached = indexOfFartherWaypointReached >= indexOfNextWaypoint
        if (newWaypointReached) {
            indexOfNextWaypoint = (indexOfFartherWaypointReached + 1).coerceAtMost(route.size - 1)
        }
        return newWaypointReached
    }

    /**
     * Checks if the given [waypoint] is reached (as stated above, if it's inside [currentRoom]).
     */
    protected open fun isReached(waypoint: Euclidean2DPosition, currentRoom: ConvexPolygon): Boolean =
        currentRoom.contains(waypoint)
}
