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
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.apache.commons.math3.util.FastMath

/**
 * An [AbstractNavigationAction] working with euclidean spaces. This action accepts
 * an [Euclidean2DEnvironmentWithGraph] whose graph features [ConvexPolygon]al nodes
 * and [Euclidean2DPassage]s as edges.
 */
open class BaseEuclideanNavigationAction<T, N : Euclidean2DConvexShape, E>(
    override val environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    /**
     * When crossing [Euclidean2DPassage]s, the pedestrian is pushed away from the wall of
     * a quantity equal to this factor * the width of the passage. This is performed to prevent
     * the pedestrian from moving attached to the wall. This factor must be in [0.0, 0.5).
     */
    private val wallRepulsionFactor: Double = DEFAULT_WALL_REPULSION_FACTOR
) : AbstractNavigationAction
<T, Euclidean2DPosition, Euclidean2DTransformation, N, E, ConvexPolygon, Euclidean2DPassage>(
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

    /**
     * Defined when crossing a door.
     */
    private var targetDoor: Euclidean2DPassage? = null

    /*
     * Avoid costly re-computations.
     */
    override val Euclidean2DPassage.target: ConvexPolygon get() = head

    override fun crossDoor(door: Euclidean2DPassage) {
        crossDoor(door, computeCrossingPoints(door))
        this.targetDoor = door
    }

    private fun computeCrossingPoints(door: Euclidean2DPassage) = door
        .copy(passageShapeOnTail = door.passageShapeOnTail.shrunk(wallRepulsionFactor))
        .crossingPoints(pedestrianPosition)

    override fun moving() {
        super.moving()
        if (state == State.MOVING_TO_CROSSING_POINT_1) {
            /*
             * Re-computes crossing points as pedestrian position may have changed, this allows more 
             * natural movements.
             */
            crossingPoints = computeCrossingPoints(targetDoor.orFail())
        }
    }

    override fun nextPosition(): Euclidean2DPosition {
        update()
        return Seek2D(environment, reaction, pedestrian, *desiredPosition.coordinates).nextPosition
    }

    @Suppress("UNCHECKED_CAST") // as? operastor is safe
    override fun cloneAction(n: Node<T>?, r: Reaction<T>?): Action<T> {
        require(n as? OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E> != null) {
            "node not compatible, required: " + pedestrian.javaClass + ", found: " + n?.javaClass
        }
        n as OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>
        require(r != null) { "reaction can't be null" }
        return BaseEuclideanNavigationAction(environment, r, n, wallRepulsionFactor).let {
            it.strategy = this.strategy
            it
        }
    }
}
