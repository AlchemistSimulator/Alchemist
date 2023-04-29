/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.euclidean.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import org.apache.commons.math3.util.FastMath

private typealias AbstractNavigationAction2D<T, L, R, N, E> =
AbstractNavigationAction<T, Euclidean2DPosition, Euclidean2DTransformation, L, R, N, E>

/**
 * Implementation of a [NavigationAction2D]. This action accepts an [Euclidean2DEnvironmentWithGraph] whose graph
 * contains [ConvexPolygon]al nodes and [Euclidean2DPassage]s as edges.
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 */
open class CognitiveAgentNavigationAction2D<T, L : Euclidean2DConvexShape, R>(
    override val environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    /**
     * When crossing [Euclidean2DPassage]s, the node is pushed away from the wall of
     * a quantity equal to (this factor * the width of the passage). This is performed to prevent
     * the node from moving attached to the wall. This factor must be in [0.0, 0.5).
     */
    private val wallRepulsionFactor: Double = DEFAULT_WALL_REPULSION_FACTOR,
) : AbstractNavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>(
    environment,
    reaction,
    pedestrian,
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
     * Avoid costly computations.
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
        /*
         * When moving towards a door the most convenient crossing point may change depending on the node
         * position. Recomputing the crossing points allows more natural movement (even though it's costly).
         */
        if (state == NavigationState.MOVING_TO_CROSSING_POINT_1 && currentRoom != null) {
            crossingPoints = computeCrossingPoints(targetDoor.orFail())
        }
    }

    override fun nextPosition(): Euclidean2DPosition {
        update()
        return CognitiveAgentSeek2D(environment, reaction, pedestrian, desiredPosition).nextPosition
    }

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentNavigationAction2D<T, L, R> {
        val clone = CognitiveAgentNavigationAction2D<T, L, R>(
            environment,
            reaction,
            node.pedestrianProperty,
            wallRepulsionFactor,
        )
        clone.strategy = this.strategy
        return clone
    }
}
