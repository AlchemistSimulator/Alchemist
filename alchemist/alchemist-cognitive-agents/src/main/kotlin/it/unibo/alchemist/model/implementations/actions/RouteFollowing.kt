/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.OrientingAction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import java.util.function.Supplier

/**
 * An [OrientingAction] allowing to follow a given [route].
 * The [route] is a list of positions (= waypoints) that may or may not be in sight of each
 * other (i.e. the path leading from a waypoint to the next one may or may not be representable
 * as a single segment), for this reason [Pursuing] behavior is used to reach each waypoint.
 * In this context, a waypoint is considered reached when it's in sight (i.e. inside [cachedCurrentRoom]),
 * apart from the last waypoint which is actually approached by the pedestrian.
 * Cuts to the route are allowed (i.e. if the pedestrian finds a waypoint which is farther than
 * the expected next one, he/she skips all the waypoints in between).
 */
open class RouteFollowing<T, N : Euclidean2DConvexShape, E> protected constructor(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    /**
     * [routeSupplier] is used with a [lazy] delegate. The reason is that upon creation of this object
     * the environment may not be filled with nodes yet (= the pedestrian's current position may be
     * unavailable). This allows subclasses to define a route supplier which will be invoked only due
     * time (= when the pedestrian's current position will be available).
     */
    routeSupplier: Supplier<List<Euclidean2DPosition>>
) : DynamicPursuing<T, N, E>(environment, reaction, pedestrian, environment.origin()) {

    /**
     * The route to follow.
     */
    protected open val route: List<Euclidean2DPosition> by lazy { routeSupplier.get() }

    constructor(
        environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
        reaction: Reaction<T>,
        pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
        route: List<Euclidean2DPosition>
    ) : this(environment, reaction, pedestrian, Supplier { route })

    constructor(
        environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
        reaction: Reaction<T>,
        pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
        vararg route: Number
    ) : this(environment, reaction, pedestrian, route.toPositions())

    private var indexOfNextWaypoint: Int = 0

    override fun onStart() = when {
        route.isEmpty() -> state = State.ARRIVED
        else -> {
            setDestination(route[0])
            super.onStart()
        }
    }

    override fun inNewRoom() {
        if (waypointReached()) {
            setDestination(route[indexOfNextWaypoint])
        }
        super.inNewRoom()
    }

    /**
     * Checks if a new waypoint has been reached (i.e. if it's inside [cachedCurrentRoom]) and updates
     * [indexOfNextWaypoint] accordingly.
     */
    protected fun waypointReached(): Boolean = when {
        indexOfNextWaypoint >= route.size - 1 -> false
        else -> route.indexOfLast { currentRoomOrFail().customContains(it) }.let { indexOfFartherWaypointInSight ->
            /*
             * [indexOfNextWaypoint] is updated only if a farther waypoint is reached.
             */
            if (indexOfFartherWaypointInSight >= indexOfNextWaypoint) {
                indexOfNextWaypoint = (indexOfFartherWaypointInSight + 1).coerceAtMost(route.size - 1)
                true
            } else {
                false
            }
        }
    }
}
