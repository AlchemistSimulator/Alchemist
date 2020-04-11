package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.LinesIntersectionType
import it.unibo.alchemist.model.implementations.geometry.areCollinear
import it.unibo.alchemist.model.implementations.geometry.liesBetween
import it.unibo.alchemist.model.implementations.geometry.linesIntersection
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import org.danilopianini.lang.MathUtils

/**
 * Defines a segment from [first] to [second] in an euclidean bidimensional space.
 */
data class Segment2D<P : Vector2D<P>>(val first: P, val second: P) {

    /**
     * Checks whether the segment is aligned to the x axis.
     */
    val xAxisAligned: Boolean get() = MathUtils.fuzzyEquals(first.y, second.y)

    /**
     * Checks whether the segment is aligned to the y axis.
     */
    val yAxisAligned: Boolean get() = MathUtils.fuzzyEquals(first.x, second.x)

    /**
     * Checks whether the segment is axis-aligned.
     */
    val isAxisAligned: Boolean get() = xAxisAligned || yAxisAligned

    /**
     * @returns the vector representing the movement from [first] to [second].
     */
    fun toVector() = second - first

    /**
     * Computes the slope of the segment. If the two points coincide (i.e. the segment
     * [isDegenerate]), [Double.NaN] is the result.
     */
    val slope: Double get() = toVector().run { y / x }

    /**
     * Computes the intercept of the line passing through [first] and [second].
     */
    val intercept: Double get() = first.y - slope * first.x

    /**
     * Checks if its points coincide (and its length is zero).
     */
    val isDegenerate: Boolean get() = first == second

    /**
     * Checks whether the segment contains the given point.
     */
    fun contains(point: P) =
        areCollinear(first, second, point) &&
        point.x.liesBetween(first.x, second.x) &&
        point.y.liesBetween(first.y, second.y)

    /**
     * Computes the medium point of the segment.
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
     * Computes the distance between the segment and a given point.
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
}
