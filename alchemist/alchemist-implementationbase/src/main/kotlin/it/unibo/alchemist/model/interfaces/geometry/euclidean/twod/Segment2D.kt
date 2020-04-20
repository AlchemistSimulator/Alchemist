package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.geometry.areCollinear
import it.unibo.alchemist.model.implementations.geometry.liesBetween
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
     * Mutates an edge to a vector. In particular, the vector representing the
     * movement from the first point to the second point of the edge.
     */
    fun toVector() = second - first

    /**
     * Computes the slope of the line passing through a couple of points.
     * If the points coincide NaN is the result.
     */
    val slope: Double get() = toVector().run { y / x }

    /**
     * An edge is degenerate if its points coincide (and its length is zero).
     */
    val degenerate: Boolean get() = first == second

    /**
     * Checks whether the segment (represented by a pair of positions)
     * contains the given point.
     */
    fun contains(other: P) =
        areCollinear(first, second, other) &&
        other.x.liesBetween(first.x, second.x) &&
        other.y.liesBetween(first.y, second.y)

    /**
     * Computes the medium point of the current segment.
     */
    val midPoint get() = Euclidean2DPosition((first.x + second.x) / 2, (first.y + second.y) / 2)

    /**
     * Finds the other of the segment which is closest to the provided position.
     */
    fun closestPointTo(other: P): P {
        return when {
            degenerate -> first
            contains(other) -> other
            else -> {
                val m1 = slope
                val intersection: P = when {
                    m1.isInfinite() -> first.newFrom(first.x, other.y)
                    MathUtils.fuzzyEquals(m1, 0.0) -> first.newFrom(other.x, first.y)
                    else -> {
                        val q1 = first.y - m1 * first.x
                        val m2 = -1 / m1
                        val q2 = other.y - m2 * other.x
                        val x = (q2 - q1) / (m1 - m2)
                        val y = m1 * x + q1
                        first.newFrom(x, y)
                    }
                }
                when {
                    contains(intersection) -> intersection
                    (first - other).magnitude < (second - other).magnitude -> first
                    else -> second
                }
            }
        }
    }

    private fun Vector2D<*>.toEuclidean2D(): Euclidean2DPosition =
        if (this is Euclidean2DPosition) this else Euclidean2DPosition(first.x, first.y)

    /**
     * Computes the distance between the current segment and a given point.
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
