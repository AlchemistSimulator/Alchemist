package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.implementations.geometry.areCollinear
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.implementations.geometry.fuzzyIn
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.intersectAsLines
import it.unibo.alchemist.model.implementations.geometry.rangeFromUnordered
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import org.danilopianini.lang.MathUtils
import java.lang.IllegalArgumentException

/**
 * Defines a segment from [first] to [second] in an euclidean bidimensional space.
 */
data class Segment2D<P : Vector2D<P>>(val first: P, val second: P) {

    /**
     * The length of the segment.
     */
    val length: Double = toVector().magnitude

    /**
     * Indicates if the segment is aligned to the x axis.
     */
    val xAxisAligned: Boolean get() = MathUtils.fuzzyEquals(first.y, second.y)

    /**
     * Indicates if the segment is aligned to the y axis.
     */
    val yAxisAligned: Boolean get() = MathUtils.fuzzyEquals(first.x, second.x)

    /**
     * Indicates if the segment is axis-aligned.
     */
    val isAxisAligned: Boolean get() = xAxisAligned || yAxisAligned

    /**
     * The slope of the segment. If the two points coincide, this is [Double.NaN].
     */
    val slope: Double get() = Double.NaN.takeIf { isDegenerate } ?: toVector().run { y / x }

    /**
     * The intercept of the line passing through [first] and [second].
     */
    val intercept: Double get() = first.y - slope * first.x

    /**
     * A segment is degenerate if its points coincide.
     */
    val isDegenerate: Boolean get() =
        MathUtils.fuzzyEquals(first.x, second.x) && MathUtils.fuzzyEquals(first.y, second.y)

    /**
     * The medium point of the segment.
     */
    val midPoint get() = Euclidean2DPosition((first.x + second.x) / 2, (first.y + second.y) / 2)

    /**
     * @returns a shrunk version of the segment, [factor] is a percentage in [0, 0.5] indicating how much
     * the segment should be reduced on each size.
     */
    fun shrunk(factor: Double): Segment2D<P> = when (factor) {
        !in 0.0..0.5 -> throw IllegalArgumentException("$factor not in [0, 0.5]")
        else -> copy(
            first = first + (second - first).resized(factor * length),
            second = second + (first - second).resized(factor * length)
        )
    }

    /**
     * @returns the vector representing the movement from [first] to [second].
     */
    fun toVector() = second - first

    /**
     * Checks if the segment contains a [point]. Doubles are not directly compared,
     * [MathUtils.fuzzyEquals] is used instead.
     */
    fun contains(point: P) =
        areCollinear(first, second, point) &&
        point.x fuzzyIn rangeFromUnordered(first.x, second.x) &&
        point.y fuzzyIn rangeFromUnordered(first.y, second.y)

    /**
     * Finds the point of the segment which is closest to the provided position.
     */
    fun closestPointTo(point: P): P =
        when {
            isDegenerate -> first
            contains(point) -> point
            else -> {
                /*
                 * Intersection between the line defined by the segment and the line
                 * perpendicular to the segment passing through the given point.
                 */
                val intersection = intersectAsLines(Segment2D(point, point + toVector().normal()))
                require(intersection is Intersection2D.SinglePoint<P>) {
                    "Bug in Alchemist geometric engine, found in ${this::class.qualifiedName}"
                }
                when {
                    contains(intersection.point) -> intersection.point
                    first.distanceTo(intersection.point) < second.distanceTo(intersection.point) -> first
                    else -> second
                }
            }
        }

    /**
     * Computes the distance between the segment and a given [point].
     */
    fun distanceTo(point: P) = closestPointTo(point).distanceTo(point)

    /**
     * Computes the (minimum) distance between two segments.
     */
    fun distanceTo(other: Segment2D<P>): Double =
        mutableListOf(
            distanceTo(other.first),
            distanceTo(other.second),
            other.distanceTo(first),
            other.distanceTo(second)
        ).min() ?: Double.POSITIVE_INFINITY

    /**
     * Maps the segment a [ClosedRange], this is done by extracting either the X coordinates or
     * the Y coordinates of the two endpoints of the segment. [getXCoords] indicates which pair
     * of coordinates should be extracted.
     * This can be useful e.g. to represent portions of axis-aligned segments without creating
     * new ones.
     */
    fun toRange(getXCoords: Boolean = this.xAxisAligned): ClosedRange<Double> = when {
        getXCoords -> rangeFromUnordered(first.x, second.x)
        else -> rangeFromUnordered(first.y, second.y)
    }

    /**
     * Checks whether this segment is inside a rectangular region described by an [origin]
     * point and [width] and [height] values (must be positive).
     */
    fun isInRectangle(origin: Vector2D<*>, width: Double, height: Double) =
        first.isInRectangle(origin, width, height) && second.isInRectangle(origin, width, height)

    /**
     * Finds the intersection point of two given segments. This method is able to deal with degenerate
     * and collinear segments.
     */
    fun intersectSegment(other: Segment2D<P>): Intersection2D<P> {
        if (isDegenerate || other.isDegenerate) {
            val degenerate = takeIf { it.isDegenerate } ?: other
            val otherSegment = other.takeIf { degenerate == this } ?: this
            return when {
                otherSegment.contains(degenerate.first) -> Intersection2D.SinglePoint(degenerate.first)
                else -> Intersection2D.None
            }
        }
        val intersection: Intersection2D<P> = intersectAsLines(other)
        return when {
            intersection is Intersection2D.SinglePoint && bothContain(this, other, intersection.point) -> intersection
            intersection is Intersection2D.Line && ! disjoint(this, other) -> {
                val sharedEndPoint = sharedEndPoint(this, other)
                /*
                 * Overlapping if there is no shared end point.
                 */
                if (sharedEndPoint == null) {
                    Intersection2D.Segment(this)
                } else {
                    Intersection2D.SinglePoint(sharedEndPoint)
                }
            }
            else -> Intersection2D.None
        }
    }

    private fun <P : Vector2D<P>> bothContain(s1: Segment2D<P>, s2: Segment2D<P>, point: P) =
        s1.contains(point) && s2.contains(point)

    /*
     * Returns false if the segments share one or more points.
     */
    private fun <P : Vector2D<P>> disjoint(s1: Segment2D<P>, s2: Segment2D<P>) =
        !(s1.contains(s2.first) || s1.contains(s2.second) || s2.contains(s1.first) || s2.contains(s1.second))

    /*
     * Returns the end point shared by the two segments, or null if they share no endpoint OR
     * if they share more than one point (i.e. they overlap).
     */
    private fun <P : Vector2D<P>> sharedEndPoint(s1: Segment2D<P>, s2: Segment2D<P>): P? {
        val fuzzyEquals: (P, P) -> Boolean = { first, second ->
            MathUtils.fuzzyEquals(first.x, second.x) && MathUtils.fuzzyEquals(first.y, second.y)
        }
        return when {
            fuzzyEquals(s1.first, s2.first) && !s1.contains(s2.second) -> s1.first
            fuzzyEquals(s1.first, s2.second) && !s1.contains(s2.first) -> s1.first
            fuzzyEquals(s1.second, s2.first) && !s1.contains(s2.second) -> s1.second
            fuzzyEquals(s1.second, s2.second) && !s1.contains(s1.first) -> s1.second
            else -> null
        }
    }
}
