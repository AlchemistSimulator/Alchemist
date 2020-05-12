/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import java.lang.IllegalStateException
import java.util.Optional

/**
 * An abstract [NavigationAction], taking care of properly moving the pedestrian in the
 * environment while delegating the decision on where to move it to a [NavigationStrategy].
 */
abstract class AbstractNavigationAction<T, P, A, N, E, M, F>(
    override val environment: EnvironmentWithGraph<*, T, P, A, M, F>,
    override val reaction: Reaction<T>,
    final override val pedestrian: OrientingPedestrian<T, P, A, N, E>
) : AbstractSteeringAction<T, P>(environment, reaction, pedestrian),
    NavigationAction<T, P, A, N, E, M, F>
    where
        P : Position<P>, P : Vector<P>,
        A : GeometricTransformation<P>,
        N : ConvexGeometricShape<P, A>,
        M : ConvexGeometricShape<P, A> {

    /**
     * The strategy used to navigate the environment.
     */
    protected open lateinit var strategy: NavigationStrategy<T, P, A, N, E, M, F>

    /**
     * The position of the [pedestrian] in the [environment], this is cached and updated
     * every time [update] is called so as to avoid potentially costly re-computations.
     */
    override lateinit var pedestrianPosition: P

    /**
     * The room (= environment's area) the [pedestrian] is into, this is cached and updated
     * every time [update] is called so as to avoid potentially costly re-computations.
     */
    override var currentRoom: Optional<M> = Optional.empty()

    /**
     * Gets [currentRoom] or throws an [IllegalStateException].
     */
    protected fun currentRoomOrFail(): M = currentRoom.orElseThrow {
        IllegalStateException("current room should be defined in $state")
    }

    /**
     * Updates [pedestrianPosition] and [currentRoom], this can be costly. 
     * Depending on how [ConvexGeometricShape.contains] manage points on the boundary, the pedestrian could
     * be inside two (adjacent) rooms at once. This can happen in two cases:
     * - when in [State.MOVING_TO_CROSSING_POINT_1] or [State.MOVING_TO_FINAL] and the pedestrian is moving
     * on [previousRoom]'s boundary. In such case [previousRoom] is used.
     * - when crossing a door or in [State.NEW_ROOM] and [expectedNewRoom] is adjacent to [previousRoom].
     * In such case [expectedNewRoom] is used.
     * Otherwise the first room containing [pedestrianPosition] is used.
     */
    protected open fun updateCachedVariables() {
        pedestrianPosition = environment.getPosition(pedestrian)
        currentRoom = Optional.ofNullable(when {
            (state == State.MOVING_TO_CROSSING_POINT_1 || state == State.MOVING_TO_FINAL) &&
                previousRoom.orFail().contains(currentPosition) -> previousRoom
            (state == State.MOVING_TO_CROSSING_POINT_2 || state == State.CROSSING_DOOR || state == State.NEW_ROOM) &&
                expectedNewRoom?.contains(currentPosition) ?: false -> expectedNewRoom
            else -> environment.graph.vertexSet().firstOrNull { it.contains(pedestrianPosition) }
        })
    }

    /**
     * Minimum distance to consider a target reached. Using zero (even with fuzzy
     * equals) may lead to some boundary cases in which the pedestrian remains blocked
     * due to how the environment manage collisions at present. This workaround allows
     * to specify a minimum distance which is dependent on the pedestrian shape. In the
     * future, something better could be done.
     */
    protected val minDistance: Double = pedestrian.shape.diameter

    /**
     * @returns true if the distance to [pedestrianPosition] is smaller than or equal to [minDistance].
     */
    protected open fun P.isReached(): Boolean = distanceTo(pedestrianPosition) <= minDistance

    protected enum class State {
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
         * When the second crossing point [isReached] (see [crossDoor]), the pedestrian may still be outside
         * any room. In such case it moves towards [expectedNewRoom] centroid until he/she enters a room.
         */
        CROSSING_DOOR,
        /**
         * Moving to the final destination, which is inside [currentRoom] (this means it can be directly
         * approached as no obstacle is placed in between).
         */
        MOVING_TO_FINAL,
        ARRIVED
    }
    protected var state: State = State.START
    /**
     * Caches the room the pedestrian is into when he/she starts moving.
     * When the pedestrian is crossing a door, it contains the room being left. When in [State.MOVING_TO_FINAL],
     * it contains the room the pedestrian was (and should be) into (it's used to detect if the pedestrian ended
     * up in an unexpected room while moving).
     */
    protected var previousRoom: M? = null
    /**
     * Defined when crossing a door. See [crossDoor].
     */
    protected var crossingPoints: Pair<P, P>? = null
    /**
     * Defined when crossing a door.
     */
    protected var expectedNewRoom: M? = null
    /**
     * Defined in [State.MOVING_TO_FINAL].
     */
    protected var finalDestination: P? = null

    /**
     * @returns a the non-null value of a nullable variable or throws an [IllegalStateException].
     */
    protected open fun <T> T?.orFail(error: String = "internal error: variable must be defined in $state"): T =
        this ?: throw IllegalStateException(error)

    protected open fun onStart() {
        state = when {
            currentRoom.isPresent -> State.NEW_ROOM
            /*
             * If the pedestrian cannot locate itself inside any room on start, it simply won't move.
             */
            else -> State.ARRIVED
        }
    }

    /**
     * @returns all the doors (= passages/edges) outgoing from the current room.
     */
    override fun doorsInSight(): List<F> = emptyList<F>()
        .takeUnless { currentRoom.isPresent }
        ?: environment.graph.outgoingEdgesOf(currentRoom.get()).toList()

    /**
     * The target of a directed edge of the environment's graph.
     */
    protected open val F.target: M get() = environment.graph.getEdgeTarget(this)

    /**
     * Moves the pedestrian across the provided [door] (which must be among [doorsInSight]).
     * Since connected rooms may be non-adjacent, a pair of [crossingPoints] has to be provided:
     * - the first point must belong to the current room's boundary and will be reached first,
     * - the second point must belong to the next room's boundary and will be pursued after 
     * reaching the former one. [crossingPoints] may coincide if the two rooms are adjacent.
     */
    protected open fun crossDoor(door: F, crossingPoints: Pair<P, P>) {
        require(doorsInSight().contains(door)) { "$door is not in sight" }
        state = State.MOVING_TO_CROSSING_POINT_1
        this.previousRoom = currentRoomOrFail()
        this.crossingPoints = crossingPoints
        this.expectedNewRoom = door.target
    }

    override fun moveToFinal(destination: P) {
        require(currentRoomOrFail().contains(destination)) { "$destination is not in $currentRoom" }
        state = State.MOVING_TO_FINAL
        this.previousRoom = currentRoomOrFail()
        this.finalDestination = destination
    }

    protected open fun moving() {
        currentRoom.takeIf { it.isPresent && it.get() != previousRoom }?.ifPresent { newRoom ->
            when (newRoom) {
                expectedNewRoom -> state = State.NEW_ROOM
                else -> strategy.inUnexpectedNewRoom(previousRoom.orFail(), expectedNewRoom.orFail(), newRoom)
            }
        } ?: let {
            if (desiredPosition.isReached()) {
                state = when (state) {
                    State.MOVING_TO_CROSSING_POINT_1 -> {
                        when {
                            /*
                             * Short-cut to save time.
                             */
                            crossingPoints.orFail().run { first == second } -> State.CROSSING_DOOR
                            else -> State.MOVING_TO_CROSSING_POINT_2
                        }
                    }
                    State.MOVING_TO_CROSSING_POINT_2 -> State.CROSSING_DOOR
                    State.MOVING_TO_FINAL -> State.ARRIVED
                    else -> state
                }
            }
        }
    }

    /**
     * The position the pedestrian wants to reach.
     */
    val desiredPosition: P get() = when (state) {
        State.MOVING_TO_CROSSING_POINT_1 -> crossingPoints.orFail().first
        State.MOVING_TO_CROSSING_POINT_2 -> crossingPoints.orFail().second
        State.CROSSING_DOOR -> expectedNewRoom.orFail().centroid
        State.MOVING_TO_FINAL -> finalDestination.orFail()
        /*
         * Always up to date current position.
         */
        else -> environment.getPosition(pedestrian)
    }

    /**
     * Updates the internal state (and all the related variables) but does not move the pedestrian.
     */
    open fun update() {
        updateCachedVariables()
        when (state) {
            State.START -> onStart()
            State.NEW_ROOM -> currentRoomOrFail().let {
                pedestrian.registerVisit(it)
                strategy.inNewRoom(it)
            }
            in State.MOVING_TO_CROSSING_POINT_1..State.MOVING_TO_FINAL -> moving()
            /*
             * Arrived.
             */
            else -> {}
        }
    }
}
