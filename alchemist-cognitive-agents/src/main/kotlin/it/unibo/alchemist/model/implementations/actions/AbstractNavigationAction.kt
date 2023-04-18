/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.AbstractNavigationAction.NavigationState.ARRIVED
import it.unibo.alchemist.model.implementations.actions.AbstractNavigationAction.NavigationState.CROSSING_DOOR
import it.unibo.alchemist.model.implementations.actions.AbstractNavigationAction.NavigationState.MOVING_TO_CROSSING_POINT_1
import it.unibo.alchemist.model.implementations.actions.AbstractNavigationAction.NavigationState.MOVING_TO_CROSSING_POINT_2
import it.unibo.alchemist.model.implementations.actions.AbstractNavigationAction.NavigationState.MOVING_TO_FINAL
import it.unibo.alchemist.model.implementations.actions.AbstractNavigationAction.NavigationState.NEW_ROOM
import it.unibo.alchemist.model.implementations.actions.AbstractNavigationAction.NavigationState.START
import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.OccupiesSpaceProperty
import it.unibo.alchemist.model.interfaces.properties.OrientingProperty
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * An abstract [NavigationAction], taking care of properly moving the node in the
 * environment while delegating the decision on where to move it to a [NavigationStrategy].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the node is into.
 * @param A the transformations supported by the shapes in this space.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 * @param N the type of nodes of the navigation graph provided by the [environment].
 * @param E the type of edges of the navigation graph provided by the [environment].
 */
abstract class AbstractNavigationAction<T, P, A, L, R, N, E>(
    override val environment: EnvironmentWithGraph<*, T, P, A, N, E>,
    override val reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian),
    NavigationAction<T, P, A, L, R, N, E>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A> {

    override val navigatingNode = node

    /**
     * The strategy used to navigate the environment.
     */
    protected open lateinit var strategy: NavigationStrategy<T, P, A, L, R, N, E>

    /**
     * The position of the [navigatingNode] in the [environment], this is cached and updated
     * every time [update] is called so as to avoid potentially costly re-computations.
     */
    override lateinit var pedestrianPosition: P

    /**
     * The room (= environment's area) the [navigatingNode] is into, this is cached and updated
     * every time [update] is called so as to avoid potentially costly re-computations.
     */
    override var currentRoom: N? = null

    /**
     * Minimum distance to consider a target reached. Using zero (even with fuzzy equals) may lead to some
     * boundary cases in which the node remains blocked due to how the environment manage collisions
     * at present. This workaround allows to specify a minimum distance which is dependent on the node
     * shape. In the future, something better could be done.
     */
    protected val minDistance: Double = node.asProperty<T, OccupiesSpaceProperty<T, P, A>>().shape.diameter

    /**
     * @returns true if the distance to [pedestrianPosition] is smaller than or equal to [minDistance].
     */
    protected open fun P.isReached(): Boolean = distanceTo(pedestrianPosition) <= minDistance

    /**
     * The navigation state.
     */
    protected var state: NavigationState = START

    /**
     * Caches the room the node is into when he/she starts moving. When the node is crossing a door, it
     * contains the room being left. When in [NavigationState.MOVING_TO_FINAL], it contains the room the node
     * was (and should be) into. It's used to detect if the node ended up in an unexpected room while moving.
     */
    protected var previousRoom: N? = null

    /**
     * Defined when crossing a door. See [crossDoor].
     */
    protected var crossingPoints: Pair<P, P>? = null

    /**
     * Defined when crossing a door.
     */
    protected var expectedNewRoom: N? = null

    /**
     * Defined in [NavigationState.MOVING_TO_FINAL].
     */
    protected var finalDestination: P? = null

    /**
     * @returns the non-null value of a nullable variable or throws an [IllegalStateException] with a meaningful
     * message.
     */
    protected fun <T> T?.orFail(): T = checkNotNull(this) { "internal error: variable must be defined in $state" }

    /**
     * Updates [pedestrianPosition] and [currentRoom], this can be costly.
     * Depending on how [ConvexGeometricShape.contains] manage points on the boundary, the node could
     * be inside two (adjacent) rooms at once. This can happen in two cases:
     * - when in [NavigationState.MOVING_TO_CROSSING_POINT_1] or [NavigationState.MOVING_TO_FINAL] and the node
     * is moving on [previousRoom]'s boundary. In such case [previousRoom] is used.
     * - when crossing a door or in [NavigationState.NEW_ROOM] and [expectedNewRoom] is adjacent to [previousRoom].
     * In such case [expectedNewRoom] is used.
     * Otherwise the first room containing [pedestrianPosition] is used.
     */
    protected open fun updateCachedVariables() {
        pedestrianPosition = environment.getPosition(navigatingNode)
        currentRoom = when {
            (state == MOVING_TO_CROSSING_POINT_1 || state == MOVING_TO_FINAL) &&
                previousRoom.orFail().contains(pedestrianPosition) ->
                previousRoom
            (state == MOVING_TO_CROSSING_POINT_2 || state == CROSSING_DOOR || state == NEW_ROOM) &&
                expectedNewRoom?.contains(pedestrianPosition) ?: false ->
                expectedNewRoom
            else ->
                environment.graph.vertexSet().firstOrNull { it.contains(pedestrianPosition) }
        }
    }

    /**
     * Execute on navigation start.
     */
    protected open fun onStart() {
        state = when {
            currentRoom != null -> NEW_ROOM
            /*
             * If the node cannot locate itself inside any room on start, it simply won't move.
             */
            else -> ARRIVED
        }
    }

    /**
     * @returns all the doors (= passages/edges) outgoing from the current room.
     */
    override fun doorsInSight(): List<E> =
        currentRoom?.let { environment.graph.outgoingEdgesOf(it).toList() }.orEmpty()

    /**
     * The target of a directed edge of the environment's graph.
     */
    protected open val E.target: N get() = environment.graph.getEdgeTarget(this)

    /**
     * Moves the node across the provided [door], which must be among [doorsInSight].
     * Since connected rooms may be non-adjacent, a pair of [crossingPoints] has to be provided:
     * - the first point must belong to the current room's boundary and will be reached first,
     * - the second point must belong to the next room's boundary and will be pursued after
     * reaching the former one. [crossingPoints] may coincide if the two rooms are adjacent.
     */
    protected open fun crossDoor(door: E, crossingPoints: Pair<P, P>) {
        require(doorsInSight().contains(door)) { "$door is not in sight" }
        state = MOVING_TO_CROSSING_POINT_1
        this.previousRoom = currentRoom.orFail()
        this.crossingPoints = crossingPoints
        this.expectedNewRoom = door.target
    }

    override fun moveToFinal(destination: P) {
        require(currentRoom.orFail().contains(destination)) { "$destination is not in $currentRoom" }
        state = MOVING_TO_FINAL
        this.previousRoom = currentRoom.orFail()
        this.finalDestination = destination
    }

    protected open fun moving() {
        currentRoom?.takeIf { it != previousRoom.orFail() }?.let { newRoom ->
            return when (newRoom) {
                expectedNewRoom.orFail() -> state = NEW_ROOM
                else -> strategy.inUnexpectedNewRoom(previousRoom.orFail(), expectedNewRoom.orFail(), newRoom)
            }
        }
        if (desiredPosition.isReached()) {
            state = when (state) {
                MOVING_TO_CROSSING_POINT_1 ->
                    /*
                     * Short-cut to save time.
                     */
                    MOVING_TO_CROSSING_POINT_2
                        .takeUnless { crossingPoints.orFail().run { first == second } }
                        ?: CROSSING_DOOR
                MOVING_TO_CROSSING_POINT_2 -> CROSSING_DOOR
                MOVING_TO_FINAL -> ARRIVED
                else -> state
            }
        }
    }

    /**
     * The position the node wants to reach.
     */
    val desiredPosition: P get() = when (state) {
        MOVING_TO_CROSSING_POINT_1 -> crossingPoints.orFail().first
        MOVING_TO_CROSSING_POINT_2 -> crossingPoints.orFail().second
        CROSSING_DOOR -> expectedNewRoom.orFail().centroid
        MOVING_TO_FINAL -> finalDestination.orFail()
        /*
         * Always up to date current position.
         */
        else -> environment.getPosition(navigatingNode)
    }

    /**
     * Updates the internal state but does not move the node.
     */
    open fun update() {
        updateCachedVariables()
        when (state) {
            START -> onStart()
            NEW_ROOM -> currentRoom.orFail().let {
                navigatingNode.asProperty<T, OrientingProperty<T, P, A, L, N, E>>()
                    .registerVisit(it)
                strategy.inNewRoom(it)
            }
            in MOVING_TO_CROSSING_POINT_1..MOVING_TO_FINAL -> moving()
            /*
             * Arrived.
             */
            else -> Unit
        }
    }

    protected enum class NavigationState {
        START,
        NEW_ROOM,

        /**
         * Moving towards the first crossing point (see [crossDoor]).
         */
        MOVING_TO_CROSSING_POINT_1,

        /**
         * Moving towards the second crossing point (see [crossDoor]).
         */
        MOVING_TO_CROSSING_POINT_2,

        /**
         * When the second crossing point [isReached] (see [crossDoor]), the node may still be outside
         * any room. In such case it moves towards [expectedNewRoom] centroid until he/she enters a room.
         */
        CROSSING_DOOR,

        /**
         * Moving to the final destination, which is inside [currentRoom] (this means it can be directly
         * approached as no obstacle is placed in between).
         */
        MOVING_TO_FINAL,
        ARRIVED,
    }
}
