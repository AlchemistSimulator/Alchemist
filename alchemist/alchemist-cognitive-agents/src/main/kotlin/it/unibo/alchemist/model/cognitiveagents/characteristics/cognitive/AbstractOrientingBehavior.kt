/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import java.lang.IllegalStateException
import kotlin.math.pow

/**
 * The orienting behavior of an [OrientingPedestrian].
 * This class obtains a route from the pedestrian's cognitive map and exploits that and the
 * other spatial information available to navigate the environment towards (or in search of)
 * a destination.
 * This is a finite state machine, whose only public method is [update]. As stated in its
 * documentation, [update] doesn't move the pedestrian, but only refreshes the internal
 * state of this behavior.
 * The client should move the pedestrian using the [desiredPosition] value, the reason is
 * that this class is meant to be extended or used via composition to obtain implementations
 * of the orienting behavior deriving from different entities of the Alchemist meta-model,
 * such as [Action] or [Reaction].
 * Two tasks are left to the derived classes via template method, see [computeEdgeRankings]
 * and [crossingPoint].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the [environment].
 * @param F the type of edges of the navigation graph provided by the [environment].
 *
 * Since E is simply Any?, this behavior assumes no extra information is stored in the edges
 * of the cognitive map.
 */
abstract class AbstractOrientingBehavior<T, P, A, N, E, M, F>(
    /**
     * The environment the pedestrian is into.
     */
    protected open val environment: EnvironmentWithGraph<*, T, P, A, M, F>,
    /**
     * The owner of this behavior.
     */
    protected val pedestrian: OrientingPedestrian<T, P, A, N, E>
) where
        P : Position<P>, P : Vector<P>,
        A : GeometricTransformation<P>,
        N : ConvexGeometricShape<P, A>,
        M : ConvexGeometricShape<P, A>,
        /*
         * Force a non-null upper bound.
         */
        F : Any {

    /*
     * When navigating towards a sub-destination, such target will be considered
     * reached when the pedestrian's distance from it is <= of this quantity.
     *
     * Considering a target reached when the distance from it it's (fuzzy) equal to
     * zero may still lead to some extreme cases in which pedestrians remain blocked
     * due to how the environment manage collisions (namely, if a pedestrian wants
     * to reach a position already occupied by someone, it can't move at all, it can't
     * even approach such position). This workaround allows to specify a minDistance
     * which is not absolute, instead it's dependent on the pedestrian shape. In the
     * future, something better could be done.
     */
    private val minDistance = pedestrian.shape.diameter

    private val currentPosition: P get() = environment.getPosition(pedestrian)

    /*
     * Route to a destination obtained from the cognitive map (can be empty).
     */
    private val route: MutableList<N> by lazy {
        with(pedestrian.cognitiveMap) {
            val closerLandmarks = vertexSet()
                .sortedBy { it.centroid.distanceTo(currentPosition) }
            val closerDestinations = destinations()
                .mapNotNull { nodeContaining(it) }
                .sortedBy { it.centroid.distanceTo(currentPosition) }
            /*
             * The pedestrian will look for a path leading from his closest
             * landmark to the closest destination possible.
             */
            val dijkstra = DijkstraShortestPath(this)
            closerDestinations.map { destination ->
                closerLandmarks.mapNotNull { landmark ->
                    /*
                     * At present the cognitive map is a MST, so there's a single
                     * path between each pair of nodes. In the future, things may
                     * change and there could be more than one shortest path between
                     * two nodes. In this case, it may be preferable to choose a
                     * shortest path with the maximum number of nodes. The reason
                     * is that such path contains more detailed information
                     * regarding the route to follow.
                     */
                    dijkstra.getPath(landmark, destination)?.vertexList
                }.first()
            }.firstOrNull()?.toMutableList() ?: mutableListOf()
        }
    }
    /*
     * The room (or walkable area) the pedestrian is into.
     */
    private lateinit var currRoom: M
    /*
     * The room (or walkable area) the pedestrian is heading to, it is connected to the
     * current room.
     */
    private lateinit var nextRoom: M
    /*
     * The position the pedestrian is moving towards, which is guaranteed to be in sight
     * (i.e. no obstacle is placed between the agent and this position).
     */
    private lateinit var subdestination: P
    /*
     * The passage the pedestrian wants to cross.
     */
    private lateinit var targetDoor: F

    private enum class State {
        START,
        NEW_ROOM,
        MOVING_TO_DOOR,
        /*
         * Connected rooms are not guaranteed to be exactly adjacent, there can be some
         * distance between them. Consequently, when crossing a door it may happen that
         * the pedestrian cannot locate itself inside any room. Such case is identified
         * by CROSSING_DOOR.
         */
        CROSSING_DOOR,
        MOVING_TO_FINAL,
        ARRIVED
    }
    private var state: State = State.START

    /**
     * The position the pedestrian wants to reach, in absolute coordinates.
     */
    val desiredPosition: P get() = when (state) {
        State.MOVING_TO_DOOR, State.CROSSING_DOOR, State.MOVING_TO_FINAL -> subdestination
        else -> currentPosition
    }

    /**
     * @returns a boolean indicating whether the pedestrian is crossing a door (or passage).
     */
    val isCrossingAPassage: Boolean get() = state == State.CROSSING_DOOR

    private fun onStart() {
        state = when {
            environment.graph().nodeContaining(currentPosition) != null -> State.NEW_ROOM
            /*
             * If the pedestrian cannot locate itself inside any room on start, it simply won't move.
             */
            else -> State.ARRIVED
        }
    }

    private fun inNewRoom() {
        val newRoom = environment.graph().nodeContaining(currentPosition)
            ?: throw IllegalStateException("pedestrian is not inside a room")
        pedestrian.registerVisit(newRoom)
        if (::nextRoom.isInitialized) {
            if (nextRoom.contains(currentPosition)) {
                currRoom = nextRoom
            }
            /*
             * We should have reached nextRoom, but we are in a different room (newRoom),
             * in such case we return back to the previous room (stored in currRoom), so
             * as to correctly follow our route.
             * In the future, something more sophisticated could be done (e.g. recomputing
             * the route).
             */
            else {
                val prevRoom = currRoom
                currRoom = newRoom
                val doorsLeadingBack = doorsInSight().filter { it.head == prevRoom }
                moveToClosestDoor(doors = doorsLeadingBack)
                return
            }
        } else {
            currRoom = newRoom
        }
        environment.graph().destinationsWithin(currRoom).let { destinationsInSight ->
            if (destinationsInSight.isNotEmpty()) {
                route.clear()
                subdestination = destinationsInSight.first()
                state = State.MOVING_TO_FINAL
                return
            }
        }
        updateRoute()
        val rankings = computeRankingsOrNull()
        val bestDoor = doorsInSight()
            .partition { environment.graph().containsAnyDestination(it.head) }
            .let { (exits, otherDoors) ->
                /*
                 * If we discover any exit along the way, we just take it. In case of multiple
                 * exits, we use [weightExit].
                 */
                exits.minBy { weightExit(it) }
                    ?: otherDoors.minWith(compareBy({ weight(it, rankings?.get(it)) }, {
                        /*
                         * nearest door heuristic
                         */
                        crossingPoint(it).distanceTo(currentPosition)
                    }))
            }
        if (bestDoor != null) {
            moveToDoor(bestDoor)
        }
        /*
         * Closed room, we can't move anywhere.
         */
        else {
            state = State.ARRIVED
        }
    }

    /*
     * Picks the closest door among the provided ones and sets all the state
     * variables so as to move to the selected door.
     * noDoorAction is called if no door could be found, defaults to nothing.
     */
    private fun moveToClosestDoor(
        doors: Collection<F>,
        noDoorAction: () -> Unit = {}
    ): Unit = doors
        .map { it to crossingPoint(it) }
        .minBy { it.second.distanceTo(currentPosition) }
        ?.let { (closestDoor, crossingPoint) -> moveToDoor(closestDoor, crossingPoint) }
        ?: noDoorAction()

    /*
     * Sets all the state variables so as to move to the selected door.
     */
    private fun moveToDoor(door: F, crossingPoint: P = crossingPoint(door)) {
        nextRoom = door.head
        subdestination = crossingPoint
        targetDoor = door
        state = State.MOVING_TO_DOOR
    }

    /*
     * If an element of the route is in sight, updates the route removing all the
     * elements up to the one encountered.
     */
    private fun updateRoute() =
        repeat(route.indexOfLast { currRoom.contains(it.centroid) } + 1) {
            route.removeAt(0)
        }

    private fun computeRankingsOrNull(): Map<F, Int>? =
        route.takeIf { it.isNotEmpty() }?.let { computeEdgeRankings(currRoom, route[0].centroid) }

    /*
     * The pedestrian can see all the edges outgoing from the current room.
     */
    private fun doorsInSight(): Collection<F> = environment.graph().outgoingEdgesOf(currRoom)

    private fun moving() {
        val inNewRoom = environment.graph().vertexSet()
            .any { it != currRoom && it.contains(currentPosition) }
        if (inNewRoom) {
            state = State.NEW_ROOM
        } else if (state != State.CROSSING_DOOR) {
            val arrived = currentPosition.distanceTo(subdestination) <= minDistance
            if (state == State.MOVING_TO_DOOR) {
                if (arrived) {
                    /*
                     * This may cause some unrealistic deviations, a possible alternative
                     * is to move the pedestrian in the same direction it moved so far.
                     */
                    subdestination = nextRoom.centroid
                    state = State.CROSSING_DOOR
                } else {
                    /*
                     * Recomputes sub-destination
                     */
                    subdestination = crossingPoint(targetDoor)
                }
            } else if (arrived) {
                state = State.ARRIVED
            }
        }
    }

    /**
     * The orienting behavior is organised as a finite state machine, this method
     * updates the internal state of the behavior but does not move the pedestrian.
     * If the position of the pedestrian didn't change since the last call, this
     * method has no effect.
     */
    fun update() = when (state) {
        State.START -> onStart()
        State.NEW_ROOM -> inNewRoom()
        State.MOVING_TO_DOOR, State.CROSSING_DOOR, State.MOVING_TO_FINAL -> moving()
        State.ARRIVED -> {}
    }

    /**
     * Maps each edge outgoing from the given room to an integer rank indicating its
     * suitability in order to reach the provided destination (which is outside of the scope
     * of the agent). Rankings should be in [1,E], where E is the number of edges, a lower
     * rank means the correspondent edge is preferable to reach the given destination.
     *
     * When following a route provided by the cognitive map, the agent often is in the following
     * situation: he knows the spatial structure of the current room and the next sub-destination
     * he want to reach, but has no other information regarding the spatial structure between
     * his room and that position (remember that it is assumed the cognitive map provide only
     * a boolean information regarding the connection of landmarks, no extra info is stored).
     * This method should implement an algorithm allowing the pedestrian to perform an educated
     * guess of which crossing to take in order to get closer to the provided destination.
     */
    protected abstract fun computeEdgeRankings(room: M, destination: P): Map<F, Int>

    /**
     * Provided a passage (or door) the pedestrian may want to cross, this method computes the exact
     * point the pedestrian will move towards in order to cross such passage. The provided passage
     * belongs to the room the agent is into (i.e. it is in sight of the agent).
     *
     * This method is mainly about exploiting the extra data stored in the passage (for instance
     * its shape and location on the room's boundary) to determine which point the pedestrian
     * shall point towards.
     */
    protected abstract fun crossingPoint(door: F): P

    /**
     * Assigns a weight to a given edge. The one with minimum weight will be chosen and crossed.
     * @param rank is the rank given to the edge when assessing its suitability to reach the
     * next sub-destination. See [cognitiveMapFactor].
     */
    protected open fun weight(edge: F, rank: Int?): Double =
        edge.head.let { head ->
            volatileMemoryFactor(head) * cognitiveMapFactor(rank) * impasseFactor(head)
        }

    /**
     * Assigns a weight to an edge which leads to a room containing a destination (i.e. an exit).
     * When weighting exits, we may want to take into account different factors (for instance,
     * we don't care about the [cognitiveMapFactor] or the [volatileMemoryFactor]).
     * By default, we only consider the distance of the exit.
     */
    protected open fun weightExit(exit: F): Double = crossingPoint(exit).distanceTo(currentPosition)

    /*
     * This factor takes into account the information stored in the pedestrian's
     * volatile memory. It is computed as 2^k where k is the number of visits
     * to the area the edge being weighted leads to.
     */
    private fun volatileMemoryFactor(head: M) = 2.0.pow(pedestrian.volatileMemory[head] ?: 0)

    /*
     * This factor takes into account the information stored in the pedestrian's
     * cognitive map. It is computed as 1 - 0.5^r where r is the rank given to
     * the edge assessing its suitability to reach the next sub-destination.
     * If rank is null, this factor is 1 for every edge.
     */
    private fun cognitiveMapFactor(rank: Int?) = 1.0 - (rank?.let { 0.5.pow(it) } ?: 0.0)

    /*
     * This factor takes into account whereas the assessed edge leads to an impasse or not.
     */
    private fun impasseFactor(head: M) = if (isImpasse(head)) 10.0 else 1.0

    /*
     * Checks if the pedestrian KNOWS that the given area is an impasse
     * (i.e. with a single door).
     */
    private fun isImpasse(area: M): Boolean =
        pedestrian.volatileMemory.contains(area) &&
            environment.graph().outgoingEdgesOf(area).distinct().count() <= 1

    /*
     * The target (or head) of a directed edge of the environment's graph.
     */
    private val F.head: M get() = environment.graph().getEdgeTarget(this)
}
