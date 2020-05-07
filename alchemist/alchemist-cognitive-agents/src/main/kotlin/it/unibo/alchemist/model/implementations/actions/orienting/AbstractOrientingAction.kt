/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.orienting

import it.unibo.alchemist.model.implementations.actions.AbstractSteeringAction
import it.unibo.alchemist.model.interfaces.OrientingAction
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.lang.IllegalStateException

/**
 * An abstract [OrientingAction], laying the basis of a navigation algorithm based on
 * a finite state machine.
 * Variables' names are inspired to indoor environments but this class works for outdoor
 * environments as well.
 */
@SuppressWarnings("TooManyFunctions") // To solve
abstract class AbstractOrientingAction<T, P, A, N, E, M, F>(
    /**
     * The environment the pedestrian is into.
     */
    protected open val environment: EnvironmentWithGraph<*, T, P, A, M, F>,
    override val reaction: Reaction<T>,
    override val pedestrian: OrientingPedestrian<T, P, A, N, E>
) : AbstractSteeringAction<T, P>(environment, reaction, pedestrian),
    OrientingAction<T, P>
    where
        P : Position<P>, P : Vector<P>,
        A : GeometricTransformation<P>,
        N : ConvexGeometricShape<P, A>,
        M : ConvexGeometricShape<P, A> {

    /**
     * Cached position of the pedestrian, can be updated using [updateCachedVariables].
     * By default the update is performed when [update] is called, this allows to avoid
     * potentially costly re-computations.
     */
    lateinit var cachedCurrentPosition: P
        protected set

    /**
     * Cache containing the room the pedestrian is into (can be null). Similarly to
     * [cachedCurrentPosition], this can be updated using [updateCachedVariables].
     * By default the update is performed when [update] is called, this allows to avoid
     * costly re-computations.
     */
    var cachedCurrentRoom: M? = null
        protected set

    /**
     * @returns a the non-null value of a nullable variable or throws an [IllegalStateException].
     */
    protected open fun <T> T?.orFail(error: String = "internal error: variable must be defined in $state"): T =
        this ?: throw IllegalStateException(error)

    /**
     * Gets [cachedCurrentRoom] [orFail]s.
     */
    protected fun currentRoomOrFail(): M = cachedCurrentRoom.orFail("can't locate the pedestrian inside any room")

    /**
     * Checks if a room contains a position. By default, [ConvexGeometricShape.contains] is used.
     * Since that method doesn't adopt a particular definition of insideness, subclasses may
     * override this to adopt one.
     */
    protected open fun M.customContains(vector: P): Boolean = contains(vector)

    /**
     * Finds the room the pedestrian is into. By default, the first room that [customContains]
     * the [cachedCurrentPosition] is returned. Subclasses may override this to adopt different policies
     * e.g. when multiple (adjacent) rooms contain the pedestrian.
     */
    protected open fun findCurrentRoom(): M? =
        environment.graph.vertexSet().firstOrNull { it.customContains(cachedCurrentPosition) }

    /**
     * Updates [cachedCurrentPosition] and [cachedCurrentRoom] (using [findCurrentRoom]). This can be costly.
     */
    protected open fun updateCachedVariables() {
        environment.getPosition(pedestrian).let { newPos ->
            if (!::cachedCurrentPosition.isInitialized || newPos != cachedCurrentPosition) {
                cachedCurrentPosition = newPos
            }
        }
        cachedCurrentRoom = findCurrentRoom()
    }

    /**
     * A position is reached when its distance from [cachedCurrentPosition] is (fuzzy) equal to zero.
     */
    protected open fun P.isReached(): Boolean = fuzzyEquals(distanceTo(cachedCurrentPosition), 0.0)

    /**
     */
    enum class State {
        START,
        NEW_ROOM,
        MOVING_TO_DOOR,
        /**
         * Since connected rooms are not guaranteed to be exactly adjacent, when crossing a
         * door the pedestrian could be outside any room. This identify such case.
         */
        CROSSING_DOOR,
        /**
         * Moving to the final destination, which is inside [cachedCurrentRoom] (i.e. it can be directly
         * approached as no obstacle is placed in between).
         */
        MOVING_TO_FINAL,
        ARRIVED
    }
    /**
     */
    var state: State = State.START
        protected set
    /**
     * Caches the room the pedestrian is into when he/she starts moving.
     * When the pedestrian is [State.MOVING_TO_DOOR] or [State.CROSSING_DOOR], it contains the room being
     * left. When in [State.MOVING_TO_FINAL], it contains the room the pedestrian was (and should be) into
     * (it's useful to detect if the pedestrian ended up in an unexpected room while moving).
     */
    var previousRoom: M? = null
        protected set
    /**
     * Defined in [State.MOVING_TO_DOOR] and [State.CROSSING_DOOR]. See [crossDoor].
     */
    var crossingPoints: Pair<P, P>? = null
        protected set
    /**
     * Defined in [State.MOVING_TO_DOOR] and [State.CROSSING_DOOR].
     */
    var expectedNewRoom: M? = null
        protected set
    /**
     * Defined in [State.MOVING_TO_FINAL].
     */
    var finalDestination: P? = null
        protected set

    protected open fun onStart() {
        state = when {
            cachedCurrentRoom != null -> State.NEW_ROOM
            /*
             * If the pedestrian cannot locate itself inside any room on start, it simply won't move.
             */
            else -> State.ARRIVED
        }
    }

    protected abstract fun inNewRoom()

    /**
     * This is called in place of [inNewRoom] when the pedestrian ends up in an unexpected room while moving.
     */
    protected abstract fun inUnexpectedNewRoom(previousRoom: M)

    /**
     * @returns the doors (= passages/edges) the pedestrian can perceive. By default, he/she can
     * see all the edges outgoing from the current room.
     */
    protected open fun doorsInSight(): Collection<F> = emptyList<F>()
        .takeUnless { cachedCurrentRoom != null }
        ?: environment.graph.outgoingEdgesOf(cachedCurrentRoom)

    /**
     * The target of a directed edge of the environment's graph.
     */
    protected open val F.target: M get() = environment.graph.getEdgeTarget(this)

    /**
     * Sets all the state variables so as to move to the provided [door] (which must be in sight
     * as defined by [doorsInSight]).
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
        this.previousRoom = cachedCurrentRoom
        this.crossingPoints = crossingPoints
        this.expectedNewRoom = door.target
    }

    /**
     * Sets all the state variables so as to move to the provided [destination], which must be
     * inside [cachedCurrentRoom] (according to [customContains]).
     */
    protected open fun moveToFinal(destination: P) {
        require(currentRoomOrFail().customContains(destination)) { "$destination is not in $cachedCurrentRoom" }
        state = State.MOVING_TO_FINAL
        this.previousRoom = cachedCurrentRoom
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
        cachedCurrentRoom?.let { currentRoom ->
            if (currentRoom != previousRoom && currentRoom != expectedNewRoom) {
                inUnexpectedNewRoom(previousRoom.orFail())
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
            State.NEW_ROOM -> inNewRoom()
            State.MOVING_TO_DOOR, State.CROSSING_DOOR, State.MOVING_TO_FINAL -> moving()
            State.ARRIVED -> {}
        }
    }
}
