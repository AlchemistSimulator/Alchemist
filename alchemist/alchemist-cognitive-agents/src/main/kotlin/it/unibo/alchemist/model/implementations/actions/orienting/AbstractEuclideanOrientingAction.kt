/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.orienting

import it.unibo.alchemist.model.implementations.actions.Seek2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.apache.commons.math3.util.FastMath

/**
 * An [AbstractOrientingAction] working with euclidean geometry. This behavior accepts
 * an [Euclidean2DEnvironmentWithGraph] whose graph features [ConvexPolygon]al nodes and
 * [Euclidean2DPassage]s as edges.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 */
abstract class AbstractEuclideanOrientingAction<T, N : Euclidean2DConvexShape, E>(
    override val environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    /**
     * When crossing [Euclidean2DPassage]s, the pedestrian is pushed away from the wall of
     * a quantity equal to this factor * the width of the passage. This is performed to prevent
     * the pedestrian from moving attached to the wall. This factor must be in [0.0, 0.5).
     */
    private val wallRepulsionFactor: Double = DEFAULT_WALL_REPULSION_FACTOR
) :
AbstractOrientingAction<T, Euclidean2DPosition, Euclidean2DTransformation, N, E, ConvexPolygon, Euclidean2DPassage>(
    environment,
    reaction,
    pedestrian
) {

    companion object {
        /**
         * Empirically found to work well.
         */
        const val DEFAULT_WALL_REPULSION_FACTOR = 0.2
    }

    init {
        require(wallRepulsionFactor in 0.0..FastMath.nextAfter(0.5, 0.0)) {
            "wallRepulsionFactor must be in [0.0, 0.5) but it's $wallRepulsionFactor"
        }
    }

    /*
     * Avoid costly re-computations.
     */
    override val Euclidean2DPassage.target: ConvexPolygon get() = head

    /**
     * Delegated to [ConvexPolygon.containsBoundaryIncluded]. Including the boundary means the pedestrian
     * can be inside two rooms at once (e.g. if they're adjacent).
     */
    override fun ConvexPolygon.customContains(vector: Euclidean2DPosition): Boolean = containsBoundaryIncluded(vector)

    /**
     * Finds the current room. In two cases the pedestrian can be located inside two rooms at once (see
     * [customContains]):
     * - when in [State.MOVING_TO_DOOR] or [State.MOVING_TO_FINAL] and he/she's moving on [previousRoom]'s
     * boundary. In such case [previousRoom] is returned.
     * - when in [State.CROSSING_DOOR] or [State.NEW_ROOM] and [expectedNewRoom] is adjacent to [previousRoom].
     * In such case [expectedNewRoom] is returned.
     * Otherwise super method is used.
     */
    override fun findCurrentRoom(): ConvexPolygon? = when {
        (state == State.MOVING_TO_DOOR || state == State.MOVING_TO_FINAL) &&
            previousRoom.orFail().customContains(cachedCurrentPosition) -> previousRoom
        (state == State.CROSSING_DOOR || state == State.NEW_ROOM) &&
            /*
             * First time in NEW_ROOM [expectedNewRoom] is not initialised.
             */
            expectedNewRoom?.customContains(cachedCurrentPosition) ?: false -> expectedNewRoom
        else -> super.findCurrentRoom()
    }

    override fun nextPosition(): Euclidean2DPosition {
        update()
        return Seek2D(environment, reaction, pedestrian, *desiredPosition.coordinates).nextPosition
    }

    /**
     * Computes the distance between a passage which is in sight (= in [cachedCurrentRoom]) and the
     * pedestrian's [cachedCurrentPosition].
     */
    protected open fun Euclidean2DPassage.distanceToPedestrian(): Double =
        crossingPointOnTail(cachedCurrentPosition).distanceTo(cachedCurrentPosition)

    /**
     * Similar to [crossDoor] but takes care of computing the crossing points of an [Euclidean2DPassage].
     */
    protected open fun crossDoor(door: Euclidean2DPassage) {
        val shrunkDoor = door.copy(passageShapeOnTail = door.passageShapeOnTail.shrunk(wallRepulsionFactor))
        crossDoor(door, shrunkDoor.crossingPoints(cachedCurrentPosition))
    }

    /**
     * Allows to get back to the [previousRoom] - which must be connected to the current one -
     * crossing the nearest door in sight.
     */
    protected open fun getBackTo(previousRoom: ConvexPolygon) {
        doorsInSight()
            .asSequence()
            .filter { it.head == previousRoom }
            .minBy { it.distanceToPedestrian() }
            ?.let { crossDoor(it) }
    }
}
