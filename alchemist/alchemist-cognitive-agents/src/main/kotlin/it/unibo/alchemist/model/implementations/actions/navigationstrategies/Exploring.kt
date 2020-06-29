/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.navigationstrategies

import it.unibo.alchemist.model.interfaces.NavigationAction2D
import it.unibo.alchemist.model.interfaces.NavigationStrategy2D
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.OrientingPedestrian2D
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DPassage
import kotlin.math.abs
import kotlin.math.pow

/**
 * A [NavigationStrategy] allowing to explore the environment.
 * In order to choose which direction to take, a weighting system is used: every time the
 * pedestrian enters a new room all the visible doors are weighted, the one with minimum
 * weight is then crossed. The weighting system used here is derived from the one by
 * [Andresen et al.](https://doi.org/10.1080/23249935.2018.1432717), see [weight].
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the pedestrian's cognitive map.
 * @param R the type of edges of the pedestrian's cognitive map, representing the [R]elations between landmarks.
 */
open class Exploring<T, L : Euclidean2DConvexShape, R>(
    override val action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    /**
     * Weight assigned to known impasses, see [impasseFactor].
     */
    private val knownImpasseWeight: Double = DEFAULT_IMPASSE_WEIGHT
) : NavigationStrategy2D<T, L, R, ConvexPolygon, Euclidean2DPassage> {

    companion object {
        /**
         * Empirically found to work well (allows the pedestrian to avoid known impasses).
         */
        const val DEFAULT_IMPASSE_WEIGHT = 10.0
    }

    /**
     * Shortcut to obtain the pedestrian.
     */
    protected val pedestrian: OrientingPedestrian2D<T, L, R> get() = action.pedestrian

    /**
     * Shortcut to obtain the environment.
     */
    protected val environment get() = action.environment

    /**
     * Computes the distance between the pedestrian and a visible passage.
     */
    protected open fun Euclidean2DPassage.distanceToPedestrian(): Double = action.pedestrianPosition.let {
        crossingPointOnTail(it).distanceTo(it)
    }

    /**
     * The comparator used to determine which passage to cross, a nearest door heuristic
     * is used when multiple passages have the same weight.
     */
    protected open val comparator: Comparator<in Euclidean2DPassage> =
        compareBy({ weight(it) }, { it.distanceToPedestrian() })

    override fun inNewRoom(newRoom: ConvexPolygon) = with(action) {
        doorsInSight().minWith(comparator)?.let { crossDoor(it) }
            /*
             * Closed room.
             */
            ?: stop()
    }

    /**
     * Assigns a weight to a visible door (= passage). This weighting system is derived from the one
     * by [Andresen et al.](https://doi.org/10.1080/23249935.2018.1432717). By default, it comprises
     * three factors: [volatileMemoryFactor], [congestionFactor] and [impasseFactor].
     */
    protected open fun weight(door: Euclidean2DPassage): Double = door.head.let {
        volatileMemoryFactor(it) * congestionFactor(it) * impasseFactor(it)
    }

    /**
     * Takes into account the information stored in the pedestrian's volatile memory. It is computed
     * as 2^v where v is the number of visits to [head] (= the area the edge being weighted leads to).
     * Less visited rooms are preferred.
     */
    protected open fun volatileMemoryFactor(head: ConvexPolygon): Double =
        2.0.pow(pedestrian.volatileMemory[head] ?: 0)

    /**
     * Takes into account the congestion of [head], it is assumed that the pedestrian can estimate the
     * congestion level of a neighboring room. It is computed as 2 * [head].congestionLevel + 0.5 (less
     * crowded rooms are preferred). This function was derived empirically, observing it produces the
     * desired behavior (i.e. allows to avoid congestion) in a simple scenario (see the "congestion
     * avoidance" test of this module).
     */
    protected open fun congestionFactor(head: ConvexPolygon): Double = 2 * head.congestionLevel + 0.5

    /**
     * Takes into account whereas the assessed edge leads to a known impasse or not, known impasses
     * are given [knownImpasseWeight] (which is usually a high value, allowing to avoid them), otherwise
     * this factor assumes unitary value.
     */
    protected open fun impasseFactor(head: ConvexPolygon): Double =
        knownImpasseWeight.takeIf { head.isKnownImpasse() } ?: 1.0

    /**
     * Area occupied by pedestrians / total area of this room. Falls in [0,1].
     */
    protected open val ConvexPolygon.congestionLevel: Double get() = environment
        .getNodesWithinRange(centroid, radius)
        .asSequence()
        .filterIsInstance<Pedestrian<T, *, *>>()
        .map { environment.getPosition(it) }
        .filter { contains(it) }
        .count()
        .let { it * pedestrian.area / area }
        .coerceAtMost(1.0)

    /**
     * A rough estimation of the area of a [ConvexPolygon].
     */
    protected open val ConvexPolygon.area: Double get() = with(asAwtShape().bounds2D) { abs(width * height) }

    /**
     * A rough estimation of the area of a [Pedestrian].
     */
    protected open val Pedestrian<T, *, *>.area: Double get() = Math.PI * shape.radius.pow(2)

    /**
     * Checks if the pedestrian knows that the area is an impasse (= an area with a single door).
     */
    protected open fun ConvexPolygon.isKnownImpasse(): Boolean = pedestrian.volatileMemory.contains(this) &&
        environment.graph.outgoingEdgesOf(this).distinct().count() <= 1
}
