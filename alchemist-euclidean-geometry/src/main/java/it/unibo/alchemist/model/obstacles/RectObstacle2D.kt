/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.obstacles

import it.unibo.alchemist.model.Obstacle2D
import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.util.math.closestTo
import it.unibo.alchemist.util.math.fuzzyEquals
import it.unibo.alchemist.util.math.fuzzyGreaterEquals
import org.apache.commons.math3.util.MathArrays
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.nextTowards

/**
 * This class implements a rectangular obstacle, whose sides are parallel to the
 * cartesian axis.
 *
 * @param <V> [Vector2D] type
</V> */
class RectObstacle2D<V : Vector2D<V>> private constructor(private val backend: java.awt.geom.Rectangle2D.Double) :
    Obstacle2D<V>,
    java.awt.Shape by backend {
    override val id: Int = System.identityHashCode(this)

    /**
     * @return the minimum x coordinate
     */
    val minX: Double get() = backend.minX

    /**
     * @return the maximum x coordinate
     */
    val maxX: Double get() = backend.maxX

    /**
     * @return the minimum y coordinate
     */
    val minY: Double get() = backend.minY

    /**
     * @return the maximum y coordinate
     */
    val maxY: Double get() = backend.maxY

    /**
     * @return the width of the rectangle
     */
    val width: Double get() = backend.width

    /**
     * @return the height of the rectangle
     */
    val height: Double get() = backend.height

    constructor(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
    ) : this (
        java.awt.geom.Rectangle2D
            .Double(min(x, x + w), min(y, y + h), abs(w), abs(h)),
    )

    override fun next(start: V, end: V): V {
        val (startx, starty) = start
        val (endx, endy) = end
        val onBorders = enforceBorders(startx, starty, endx, endy)
        val finalCoordinates =
            when {
                onBorders != null -> onBorders
                else -> {
                    val intersection = nearestIntersection(start, end).coordinates
                /*
                 * Ensure the intersection is outside the boundaries. Force it to be.
                 */
                    while (contains(intersection[0], intersection[1])) {
                        intersection[0] = intersection[0].nextTowards(startx)
                        intersection[1] = intersection[1].nextTowards(starty)
                    }
                    enforceBorders(intersection[0], intersection[1], intersection[0], intersection[1])
                        ?: intersection
                }
            }
        return start.newFrom(finalCoordinates[0], finalCoordinates[1])
    }

    private fun enforceBorders(startx: Double, starty: Double, endx: Double, endy: Double): DoubleArray? {
        if (!isInsideObstacle(startx, starty)) return null

        val onLeftBorder = fuzzyEquals(startx, minX)
        val onRightBorder = fuzzyEquals(startx, maxX)
        val onBottomBorder = fuzzyEquals(starty, minY)
        val onTopBorder = fuzzyEquals(starty, maxY)

        return when {
            isOnCorner(onLeftBorder, onRightBorder, onTopBorder, onBottomBorder) ->
                enforceCornerRestrictions(
                    startx,
                    starty,
                    endx,
                    endy,
                    onLeftBorder,
                    onRightBorder,
                    onTopBorder,
                    onBottomBorder,
                )

            else ->
                enforceEdgeRestrictions(
                    startx,
                    starty,
                    endx,
                    endy,
                    onLeftBorder,
                    onRightBorder,
                    onBottomBorder,
                    onTopBorder,
                )
        }
    }

    private fun isInsideObstacle(x: Double, y: Double): Boolean = fuzzyGreaterEquals(y, minY) &&
        fuzzyGreaterEquals(maxY, y) &&
        fuzzyGreaterEquals(x, minX) &&
        fuzzyGreaterEquals(maxX, x)

    private fun isOnCorner(
        onLeftBorder: Boolean,
        onRightBorder: Boolean,
        onTopBorder: Boolean,
        onBottomBorder: Boolean,
    ): Boolean = (onLeftBorder || onRightBorder) && (onTopBorder || onBottomBorder)

    private fun enforceCornerRestrictions(
        startx: Double,
        starty: Double,
        endx: Double,
        endy: Double,
        onLeftBorder: Boolean,
        onRightBorder: Boolean,
        onTopBorder: Boolean,
        onBottomBorder: Boolean,
    ): DoubleArray {
        val angle = atan2(endy - starty, endx - startx)
        val isValidMove =
            when {
                onTopBorder && angle in 0.0..<Math.PI -> true
                onRightBorder && angle in -HALF_PI..<HALF_PI -> true
                onBottomBorder && angle in -Math.PI..<0.0 -> true
                onLeftBorder && (angle > HALF_PI || angle < -HALF_PI) -> true
                else -> false
            }
        return doubleArrayOf(if (isValidMove) endx else startx, if (isValidMove) endy else starty)
    }

    private fun enforceEdgeRestrictions(
        startx: Double,
        starty: Double,
        endx: Double,
        endy: Double,
        onLeftBorder: Boolean,
        onRightBorder: Boolean,
        onBottomBorder: Boolean,
        onTopBorder: Boolean,
    ): DoubleArray {
        val res = doubleArrayOf(endx, endy)
        when {
            onLeftBorder && endx >= minX -> res[0] = minX.nextTowards(startx)
            onRightBorder && endx <= maxX -> res[0] = maxX.nextTowards(startx)
            onBottomBorder && endy >= minY -> res[1] = minY.nextTowards(starty)
            onTopBorder && endy <= maxY -> res[1] = maxY.nextTowards(starty)
        }
        return res
    }

    override fun nearestIntersection(start: V, end: V): V {
        val startx = start.x
        val starty = start.y
        val endx = end.x
        val endy = end.y
        val nearx = closestTo(startx, maxX, minX)
        val neary = closestTo(starty, maxY, minY)
        val farx = if (nearx == maxX) minX else maxX
        val fary = if (neary == maxY) minY else maxY
        val intersectionSide1: DoubleArray = intersection(startx, starty, endx, endy, nearx, neary, nearx, fary)
        val intersectionSide2: DoubleArray = intersection(startx, starty, endx, endy, nearx, neary, farx, neary)
        val d1 = MathArrays.distance(intersectionSide1, doubleArrayOf(startx, starty))
        val d2 = MathArrays.distance(intersectionSide2, doubleArrayOf(startx, starty))
        if (d1 < d2) {
            return start.newFrom(intersectionSide1[0], intersectionSide1[1])
        }
        return start.newFrom(intersectionSide2[0], intersectionSide2[1])
    }

    override fun contains(x: Double, y: Double): Boolean = x >= minX && y >= minY && x <= maxX && y <= maxY

    override fun toString(): String = "[$minX,$minY -> $maxX,$maxY]"

    private companion object {
        private const val serialVersionUID = -3552947311155196461L
        private const val HALF_PI = Math.PI / 2

        /*
         * This code was built upon Alexander Hristov's, see:
         *
         * http://www.ahristov.com/tutorial/geometry-games/intersection-segments.html
         */
        private fun intersection(
            x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
            x3: Double,
            y3: Double,
            x4: Double,
            y4: Double,
        ): DoubleArray {
            val d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
            if (d == 0.0) {
                return doubleArrayOf(x2, y2)
            }
            /*
             * Intersection point between lines
             */
            var xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d
            var yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d
            /*
             * If a point is on a border, reduce it to the exact border
             */
            xi =
                when {
                    fuzzyEquals(xi, x3) -> x3
                    fuzzyEquals(xi, x4) -> x4
                    else -> xi
                }
            yi =
                when {
                    fuzzyEquals(yi, y3) -> y3
                    fuzzyEquals(yi, y4) -> y4
                    else -> yi
                }
            /*
             * Check if there is an actual intersection
             */
            val actualIntersection =
                intersectionOutOfRange(xi, x1, x2) ||
                    intersectionOutOfRange(xi, x3, x4) ||
                    intersectionOutOfRange(yi, y1, y2) ||
                    intersectionOutOfRange(yi, y3, y4)
            return when {
                actualIntersection -> doubleArrayOf(x2, y2)
                else -> doubleArrayOf(xi, yi)
            }
        }

        private fun intersectionOutOfRange(intersection: Double, start: Double, end: Double): Boolean {
            val min = min(start, end)
            val max = max(start, end)
            return !fuzzyGreaterEquals(intersection, min) || !fuzzyGreaterEquals(max, intersection)
        }
    }
}
