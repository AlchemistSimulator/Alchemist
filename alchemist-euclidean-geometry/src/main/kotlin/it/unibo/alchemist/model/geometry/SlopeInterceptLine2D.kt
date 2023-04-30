/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

import org.danilopianini.lang.MathUtils
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A [Line2D] represented in the slope-intercept form: y = [slope] * x + [yIntercept]. Doubles are only compared with
 * [MathUtils.fuzzyEquals].
 */
class SlopeInterceptLine2D<P : Vector2D<P>> private constructor(
    override val slope: Double,
    override val yIntercept: Double,
    override val xIntercept: Double,
    private val createPoint: (Double, Double) -> P,
) : Line2D<P> {

    /**
     * Creates a non-vertical line given its [slope] and [yIntercept].
     */
    constructor(slope: Double, yIntercept: Double, createPoint: (Double, Double) -> P) : this(
        slope,
        yIntercept,
        Double.NaN.takeIf { MathUtils.fuzzyEquals(slope, 0.0) } ?: -yIntercept / slope,
        createPoint,
    )

    /**
     * Creates a vertical line given its [xIntercept].
     */
    constructor(xIntercept: Double, createPoint: (Double, Double) -> P) :
        this(Double.NaN, Double.NaN, xIntercept, createPoint)

    companion object {
        /**
         * Creates a line from a non-degenerate [segment].
         */
        fun <P : Vector2D<P>> fromSegment(segment: Segment2D<P>): SlopeInterceptLine2D<P> = with(segment) {
            when {
                isDegenerate -> throw IllegalArgumentException("can't create a line from a degenerate segment")
                isVertical -> SlopeInterceptLine2D(first.x, first::newFrom)
                else -> {
                    val slope = toVector.run { y / x }
                    SlopeInterceptLine2D(slope, first.y - slope * first.x, first::newFrom)
                }
            }
        }

        /**
         * Solves the quadratic equation [a] * x^2 + [b] * x + [c] = 0.
         */
        private fun solveQuadraticEquation(a: Double, b: Double, c: Double): List<Double> {
            val determinant = b.pow(2) - 4 * a * c
            return when {
                determinant < 0.0 -> emptyList()
                MathUtils.fuzzyEquals(determinant, 0.0) -> listOf(-b)
                else -> listOf(-b + sqrt(determinant), -b - sqrt(determinant))
            }.map { it / (2 * a) }
        }
    }

    override val isHorizontal: Boolean = MathUtils.fuzzyEquals(slope, 0.0)

    override val isVertical: Boolean = slope.isNaN()

    override fun contains(point: P): Boolean = when {
        isVertical -> MathUtils.fuzzyEquals(xIntercept, point.x)
        else -> MathUtils.fuzzyEquals(findY(point.x), point.y)
    }

    /**
     * Solves the line equation for the given [x]. Throws an [UnsupportedOperationException] if the line [isVertical].
     */
    fun findY(x: Double): Double = when {
        isVertical -> throw UnsupportedOperationException("can't compute y coordinate for vertical line")
        else -> slope * x + yIntercept
    }

    override fun findPoint(x: Double): P = createPoint(x, findY(x))

    private fun parallelTo(other: Line2D<*>): Boolean =
        isVertical && other.isVertical || MathUtils.fuzzyEquals(slope, other.slope)

    override fun isParallelTo(other: Line2D<P>): Boolean = parallelTo(other)

    /**
     * Checks if two lines coincide.
     */
    fun coincidesWith(other: Line2D<*>): Boolean = when {
        !parallelTo(other) -> false
        isVertical -> MathUtils.fuzzyEquals(xIntercept, other.xIntercept)
        else -> MathUtils.fuzzyEquals(yIntercept, other.yIntercept)
    }

    override fun intersect(other: Line2D<P>): Intersection2D<P> = when {
        coincidesWith(other) ->
            Intersection2D.InfinitePoints
        /*
         * But not coincident.
         */
        isParallelTo(other) ->
            Intersection2D.None
        else ->
            Intersection2D.SinglePoint(
                when {
                    /*
                     * other can't be vertical as lines are not parallel at this point.
                     */
                    isVertical -> other.findPoint(xIntercept)
                    other.isVertical -> findPoint(other.xIntercept)
                    else -> findPoint((other.yIntercept - yIntercept) / (slope - other.slope))
                },
            )
    }

    /**
     * Intersects a line and a circle. Radius must be positive.
     * Intersection is performed by plugging the line equation in the circle equation and solving the resulting
     * quadratic equation.
     * Circle equation: (x - [center].x)^2 + (y - [center].y)^2 = r^2.
     * Line equation: y = [slope] * x + [yIntercept] unless [isVertical], x = [xIntercept] otherwise.
     */
    override fun intersectCircle(center: P, radius: Double): Intersection2D<P> = Intersection2D.create(
        when {
            radius <= 0.0 -> throw IllegalArgumentException("radius must be > 0")
            isVertical ->
                /*
                 * The equation roots are y-coordinates.
                 */
                solveQuadraticEquation(
                    1.0,
                    -2 * center.y,
                    center.y.pow(2) + (xIntercept - center.x).pow(2) - radius.pow(2),
                ).map { createPoint(xIntercept, it) }

            else ->
                /*
                 * The equation roots are x-coordinates.
                 */
                solveQuadraticEquation(
                    1 + slope.pow(2),
                    2 * slope * (yIntercept - center.y) - 2 * center.x,
                    center.x.pow(2) + (yIntercept - center.y).pow(2) - radius.pow(2),
                ).map { findPoint(it) }
        },
    )

    /**
     * Checks if [other] is a [Line2D] and if it [coincidesWith] this one.
     */
    override fun equals(other: Any?): Boolean = this === other || other is Line2D<*> && coincidesWith(other)

    /**
     * Uses [slope], [yIntercept] and [xIntercept].
     */
    override fun hashCode(): Int = doubleArrayOf(slope, yIntercept, xIntercept).contentHashCode()
}
