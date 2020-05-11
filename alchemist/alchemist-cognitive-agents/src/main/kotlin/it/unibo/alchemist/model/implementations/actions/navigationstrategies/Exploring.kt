/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.navigationstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EuclideanNavigationAction
import it.unibo.alchemist.model.interfaces.EuclideanNavigationStrategy
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import java.awt.Shape
import kotlin.math.abs
import kotlin.math.pow

/**
 * A [NavigationStrategy] allowing to explore the environment.
 * In order to choose which direction to take, a weighting system is used: every time the
 * pedestrian enters a new room all the visible doors are weighted, the one with minimum
 * weight is then crossed. The weighting system used here is derived from the one by
 * [Andresen et al.](https://doi.org/10.1080/23249935.2018.1432717), see [weight].
 */
open class Exploring<T, N : Euclidean2DConvexShape, E>(
    override val action: EuclideanNavigationAction<T, N, E, ConvexPolygon, Euclidean2DPassage>
) : EuclideanNavigationStrategy<T, N, E, ConvexPolygon, Euclidean2DPassage> {

    /**
     * Shortcut to obtain the pedestrian.
     */
    protected val pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>
        get() = action.pedestrian

    /**
     * Computes the distance between the pedestrian and a passage in sight (= inside current room).
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
     * Assigns a weight to a passage (= door). This weighting system is derived from the one by
     * [Andresen et al.](https://doi.org/10.1080/23249935.2018.1432717). By default, it comprises
     * three factors: [volatileMemoryFactor], [congestionFactor] and [impasseFactor].
     */
    protected open fun weight(passage: Euclidean2DPassage): Double = passage.head.let {
        volatileMemoryFactor(it) * congestionFactor(it) * impasseFactor(it)
    }

    /**
     * This factor takes into account the information stored in the pedestrian's volatile memory.
     * It is computed as 2^v where v is the number of visits to the area the edge being weighted
     * leads to (in other words, less visited rooms are preferred).
     */
    protected open fun volatileMemoryFactor(head: ConvexPolygon): Double =
        2.0.pow(pedestrian.volatileMemory[head] ?: 0)

    /**
     * This factor takes into account the congestion of the room the edge being weighted leads
     * to (less crowded room are preferred). It is assumed that the pedestrian can estimate the
     * level of congestion of a neighboring room.
     */
    protected open fun congestionFactor(head: ConvexPolygon): Double = action.environment
        .getNodesWithinRange(head.centroid, head.diameter / 2)
        .filterIsInstance<Pedestrian<T>>()
        .count()
        .let { pedestrian.shape.diameter.pow(2) * it / head.asAwtShape().area() + 1 }

    /**
     * This factor takes into account whereas the assessed passage leads to a known impasse or not
     * (a higher weight is assigned to known impasses so as to avoid them).
     */
    protected open fun impasseFactor(head: ConvexPolygon): Double = if (head.isKnownImpasse()) 10.0 else 1.0

    /**
     * A rough estimation of the area of a [Shape].
     */
    protected open fun Shape.area(): Double = with(bounds2D) { abs(width * height) }

    /**
     * Checks if the pedestrian knows that the area is an impasse (= an area with a single door).
     */
    protected open fun ConvexPolygon.isKnownImpasse(): Boolean =
        pedestrian.volatileMemory.contains(this) &&
            action.environment.graph.outgoingEdgesOf(this).distinct().count() <= 1
}
