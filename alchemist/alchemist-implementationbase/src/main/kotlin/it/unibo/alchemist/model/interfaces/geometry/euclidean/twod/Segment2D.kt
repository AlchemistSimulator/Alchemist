package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.LinesIntersectionType
import it.unibo.alchemist.model.implementations.geometry.areCollinear
import it.unibo.alchemist.model.implementations.geometry.fuzzyIn
import it.unibo.alchemist.model.implementations.geometry.linesIntersection
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
     * @returns the vector representing the movement from [first] to [second].
     */
    fun toVector() = second - first

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
     * Checks if the segment contains a [point]. Doubles are not directly compared,
     * [MathUtils.fuzzyEquals] is used instead.
     */
    fun contains(point: P) =
        areCollinear(first, second, point) &&
        point.x fuzzyIn rangeFromUnordered(first.x, second.x) &&
        point.y fuzzyIn rangeFromUnordered(first.y, second.y)

    /**
     * The medium point of the segment.
     */
    val midPoint get() = Euclidean2DPosition((first.x + second.x) / 2, (first.y + second.y) / 2)

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
                val intersection = linesIntersection(this, Segment2D(point, point + toVector().normal()))
                    .let {
                        require(it.type == LinesIntersectionType.POINT && it.point.isPresent) { "internal error" }
                        it.point.get()
                    }
                when {
                    contains(intersection) -> intersection
                    first.distanceTo(intersection) < second.distanceTo(intersection) -> first
                    else -> second
                }
            }
        }

    private fun Vector2D<*>.toEuclidean2D(): Euclidean2DPosition =
        if (this is Euclidean2DPosition) this else Euclidean2DPosition(first.x, first.y)

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
}
