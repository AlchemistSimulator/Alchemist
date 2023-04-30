/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.navigationgraph

import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.geometry.Intersection2D
import it.unibo.alchemist.model.geometry.Segment2D
import it.unibo.alchemist.model.geometry.Segment2DImpl
import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.BugReporting

/**
 * Defines a passage between two [ConvexPolygon]s in an euclidean bidimensional space. The passage is oriented, which
 * means it connects [tail] to [head], but the opposite is not necessarily true. [tail] and [head] can be non-adjacent
 * (there can be some distance between them), this introduces navigation issues as agents may not know which direction
 * to follow when crossing a passage. [passageShapeOnTail] is a [Segment2D] representing the shape of the passage on
 * [tail]'s boundary (e.g. in indoor environments, the segment should represent the shape of the door between two
 * rooms). [passageShapeOnTail] must be determined so as to guarantee that [head] is reachable by throwing a ray from
 * any point of the segment in its normal direction. This solves navigation issues as it provides agents with a
 * direction to follow when crossing [Euclidean2DPassage]s (namely, the normal direction to [passageShapeOnTail]).
 */
data class Euclidean2DPassage(
    val tail: ConvexPolygon,
    val head: ConvexPolygon,
    val passageShapeOnTail: Segment2D<Euclidean2DPosition>,
) {

    init {
        with(passageShapeOnTail) {
            require(!passageShapeOnTail.isDegenerate) { "passage shape cannot be degenerate" }
            require(tail.containsBoundaryIncluded(first) && tail.containsBoundaryIncluded(second)) {
                "$passageShapeOnTail does not belong to $tail"
            }
        }
    }

    /**
     * The side of [head] which is first encountered when crossing the passage.
     */
    private val headClosestEdge: Segment2D<Euclidean2DPosition> by lazy { head.closestEdgeTo(passageShapeOnTail) }

    /**
     * Provided the [position] of an agent that may want to cross this passage, this method computes the point
     * belonging to [passageShapeOnTail] which is more convenient to cross. Note that the agent must be inside [tail].
     */
    fun crossingPointOnTail(position: Euclidean2DPosition): Euclidean2DPosition =
        with(passageShapeOnTail) {
            require(tail.containsBoundaryIncluded(position)) { "$position is not inside $tail" }
            val idealMovement = Segment2DImpl(position, head.centroid)
            /*
             * The crossing point is computed as the point belonging to the passage which is closest to the
             * intersection of the lines defined by the ideal movement and the passage itself.
             */
            closestPointTo(linesIntersectionOrFail(this, idealMovement))
        }

    /**
     * Provided the [crossingPointOnTail] that an agent has reached (or will reach), this method computes the point
     * belonging to the boundary of [head] that the agent should point towards to cross the passage (i.e. the first
     * point belonging to [head]'s boundary that is encountered by throwing a ray from [crossingPointOnTail] along
     * [passageShapeOnTail]'s normal direction).
     * Note that the returned point may not be formally contained in [head] depending on the definition of insideness
     * used by [ConvexPolygon.contains], prefer using [ConvexPolygon.containsBoundaryIncluded].
     */
    fun crossingPointOnHead(crossingPointOnTail: Euclidean2DPosition): Euclidean2DPosition =
        with(crossingPointOnTail) {
            require(tail.containsBoundaryIncluded(this)) { "$crossingPointOnTail is not contained in $tail" }
            val movement = Segment2DImpl(this, this + passageShapeOnTail.toVector.normal())
            linesIntersectionOrFail(movement, headClosestEdge)
        }

    /**
     * Provided the [position] of an agent that may want to cross this passage, this method returns a pair containing
     * both [crossingPointOnTail] and [crossingPointOnHead].
     */
    fun crossingPoints(position: Euclidean2DPosition): Pair<Euclidean2DPosition, Euclidean2DPosition> =
        crossingPointOnTail(position).let { Pair(it, crossingPointOnHead(it)) }

    private fun <V : Vector2D<V>> linesIntersectionOrFail(segment1: Segment2D<V>, segment2: Segment2D<V>): V =
        when (val intersection = segment1.toLine().intersect(segment2.toLine())) {
            is Intersection2D.SinglePoint -> intersection.point
            else -> BugReporting.reportBug("Bug in geometric engine, found in ${this::class.qualifiedName}")
        }
}
