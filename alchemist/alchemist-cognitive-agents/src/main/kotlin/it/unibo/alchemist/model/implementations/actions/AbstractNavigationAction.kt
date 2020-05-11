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
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.lang.IllegalStateException
import java.util.Optional

/**
 * An abstract [NavigationAction], taking care of properly moving the pedestrian in the
 * environment while delegating the decision on where to move it to a [NavigationStrategy].
 */
abstract class AbstractNavigationAction<T, P, A, N, E, M, F>(
    override val environment: EnvironmentWithGraph<*, T, P, A, M, F>,
    override val reaction: Reaction<T>,
    override val pedestrian: OrientingPedestrian<T, P, A, N, E>
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
     * Checks if a room contains a position. By default, [ConvexGeometricShape.contains] is used.
     * Since that method doesn't adopt a particular definition of insideness, subclasses may
     * override this to adopt one.
     */
    protected open fun M.customContains(vector: P): Boolean = contains(vector)

    /**
     * Finds the room the pedestrian is into. By default, the first room that [customContains]
     * the [pedestrianPosition] is returned. Subclasses may override this to adopt different policies
     * e.g. when multiple (adjacent) rooms contain the pedestrian.
     */
    protected open fun findCurrentRoom(): M? =
        environment.graph.vertexSet().firstOrNull { it.customContains(pedestrianPosition) }

    /**
     * Updates [pedestrianPosition] and [currentRoom] (using [findCurrentRoom]). This can be costly.
     */
    protected open fun updateCachedVariables() {
        pedestrianPosition = environment.getPosition(pedestrian)
        currentRoom = Optional.ofNullable(findCurrentRoom())
    }

    /**
     * A position is reached when its distance from [pedestrianPosition] is (fuzzy) equal to zero.
     */
    protected open fun P.isReached(): Boolean = fuzzyEquals(distanceTo(pedestrianPosition), 0.0)

    protected enum class State {
        START,
        NEW_ROOM,
        MOVING_TO_DOOR,
        /**
         * Since connected rooms are not guaranteed to be exactly adjacent, when crossing a
         * door the pedestrian could be outside any room. This identify such case.
         */
        CROSSING_DOOR,
        /**
         * Moving to the final destination, which is inside [currentRoom] (i.e. it can be directly
         * approached as no obstacle is placed in between).
         */
        MOVING_TO_FINAL,
        ARRIVED
    }
    protected var state: State =
        State.START
    /**
     * Caches the room the pedestrian is into when he/she starts moving.
     * When the pedestrian is [State.MOVING_TO_DOOR] or [State.CROSSING_DOOR], it contains the room being
     * left. When in [State.MOVING_TO_FINAL], it contains the room the pedestrian was (and should be) into
     * (it's useful to detect if the pedestrian ended up in an unexpected room while moving).
     */
    protected var previousRoom: M? = null
    /**
     * Defined in [State.MOVING_TO_DOOR] and [State.CROSSING_DOOR]. See [crossDoor].
     */
    protected var crossingPoints: Pair<P, P>? = null
    /**
     * Defined in [State.MOVING_TO_DOOR] and [State.CROSSING_DOOR].
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
     * - the first point must belong to current room's boundary and will be reached first,
     * - the second point must be contained in the next room (i.e. [door].[target].[customContains]
     * must be true for this point) and will be pursued after reaching the former one.
     * [crossingPoints] may coincide if the two rooms are adjacent.
     */
    protected open fun crossDoor(door: F, crossingPoints: Pair<P, P>) {
        require(doorsInSight().contains(door)) { "$door is not in sight" }
        require(door.target.customContains(crossingPoints.second)) {
            "${crossingPoints.second} is not in ${door.target}"
        }
        state = when {
            /*
             * Shortcut to save time.
             */
            crossingPoints.first == crossingPoints.second -> State.CROSSING_DOOR
            else -> State.MOVING_TO_DOOR
        }
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
        if (desiredPosition.isReached()) {
            state = when (state) {
                State.MOVING_TO_DOOR -> State.CROSSING_DOOR
                State.CROSSING_DOOR -> State.NEW_ROOM
                State.MOVING_TO_FINAL -> State.ARRIVED
                else -> throw IllegalStateException("internal error: pedestrian should be moving but is $state")
            }
        }
        /*
         * Detects if the pedestrian ended up in an unexpected room. There are other ways in which he/she
         * could get lost which are not monitored at present (e.g. if he/she goes outside the current room
         * when MOVING_TO_DOOR).
         */
        currentRoom.ifPresent { currentRoom ->
            if (currentRoom != previousRoom && currentRoom != expectedNewRoom) {
                strategy.inUnexpectedNewRoom(previousRoom.orFail(), expectedNewRoom.orFail(), currentRoom)
            }
        }
    }

    /**
     * The position the pedestrian wants to reach.
     */
    val desiredPosition: P get() = when (state) {
        State.MOVING_TO_DOOR -> crossingPoints.orFail().first
        State.CROSSING_DOOR -> crossingPoints.orFail().second
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
            State.MOVING_TO_DOOR, State.CROSSING_DOOR, State.MOVING_TO_FINAL -> moving()
            State.ARRIVED -> {}
        }
    }
}
