package it.unibo.alchemist.model.implementations.geometry.euclidean2d

import it.unibo.alchemist.model.implementations.geometry.fuzzyIn
import it.unibo.alchemist.model.implementations.geometry.rangeFromUnordered
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Line2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.lang.UnsupportedOperationException

/**
 * A [Segment2D] partly delegated to [Line2D]. Doubles are only compared with [fuzzyEquals]. Properties are computed
 * only once upon creation.
 */
data class BaseSegment2D<P : Vector2D<P>>(override val first: P, override val second: P) : Segment2D<P> {

    override val toVector: P = second - first

    override val length: Double = toVector.magnitude

    override val isDegenerate: Boolean = fuzzyEquals(first, second)

    /**
     * Indicates if the segment is aligned to the x-axis. If [isDegenerate] this is true.
     */
    override val isHorizontal: Boolean = fuzzyEquals(first.y, second.y)

    /**
     * Indicates if the segment is aligned to the y-axis. If [isDegenerate] this is true.
     */
    override val isVertical: Boolean = fuzzyEquals(first.x, second.x)

    override val midPoint: P = first.newFrom((first.x + second.x) / 2, (first.y + second.y) / 2)

    private val line: Line2D<P>? by lazy {
        takeIf { !isDegenerate }?.let { SlopeInterceptLine2D.fromSegment(this) }
    }

    override fun toLine(): Line2D<P> = when (line) {
        null -> throw UnsupportedOperationException("degenerate segment can't be converted to line")
        else -> line as Line2D<P>
    }

    override fun copyWith(first: P, second: P): Segment2D<P> = copy(first = first, second = second)

    override fun contains(point: P): Boolean = isCollinearWith(point) &&
        point.x fuzzyIn rangeFromUnordered(first.x, second.x) &&
        point.y fuzzyIn rangeFromUnordered(first.y, second.y)

    override fun closestPointTo(point: P): P = when {
        isDegenerate -> first
        contains(point) -> point
        else -> {
            /*
             * Intersect the line defined by this segment and the line perpendicular to this segment passing
             * through the given point.
             */
            val intersection = toLine().intersect(copyWith(point, point + toVector.normal()).toLine())
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
     * Computes the shortest distance between two segments (= the shortest distance between any two of their points).
     * This implementation uses [intersect] as intersection test (this may be a significant overhead).
     */
    override fun distanceTo(other: Segment2D<P>): Double = when {
        intersect(other) !is Intersection2D.None -> 0.0
        else -> listOf(distanceTo(other.first), distanceTo(other.second), other.distanceTo(first),
            other.distanceTo(second)).min() ?: Double.POSITIVE_INFINITY
    }

    override fun isCollinearWith(point: P): Boolean = isDegenerate || toLine().contains(point)

    override fun isCollinearWith(other: Segment2D<P>): Boolean = when {
        isDegenerate -> other.isCollinearWith(first)
        else -> isCollinearWith(other.first) && isCollinearWith(other.second)
    }

    override fun overlapsWith(other: Segment2D<P>): Boolean = isCollinearWith(other) &&
        (contains(other.first) || contains(other.second) || other.contains(first) || other.contains(second))

    override fun intersect(other: Segment2D<P>): Intersection2D<P> = when {
        isDegenerate -> Intersection2D.None.takeUnless { other.contains(first) } ?: Intersection2D.SinglePoint(first)
        other.isDegenerate -> other.intersect(this)
        isCollinearWith(other) && overlapsWith(other) -> endpointSharedWith(other)
            ?.let { Intersection2D.SinglePoint(it) }
            /*
             * Overlapping and sharing more than one point means they share a portion of segment (= infinite points).
             */
            ?: Intersection2D.InfinitePoints
        else -> Intersection2D.create(
            toLine().intersect(other.toLine()).asList.filter { contains(it) && other.contains(it) }
        )
    }

    override fun intersectCircle(center: P, radius: Double): Intersection2D<P> = when {
        isDegenerate -> Intersection2D.None
            .takeUnless { fuzzyEquals(first.distanceTo(center), radius) }
            ?: Intersection2D.SinglePoint(first)
        else -> Intersection2D.create(toLine().intersectCircle(center, radius).asList.filter { contains(it) })
    }

    /**
     * @returns the endpoint shared by the two segments, or null if they share no endpoint OR if they
     * share more than one point.
     */
    private fun endpointSharedWith(other: Segment2D<P>): P? = when {
        fuzzyEquals(first, other.first) && !contains(other.second) -> first
        fuzzyEquals(first, other.second) && !contains(other.first) -> first
        fuzzyEquals(second, other.first) && !contains(other.second) -> second
        fuzzyEquals(second, other.second) && !contains(other.first) -> second
        else -> null
    }

    companion object {
        /**
         * Checks if two points are [fuzzyEquals].
         */
        fun <P : Vector2D<P>> fuzzyEquals(a: P, b: P): Boolean = fuzzyEquals(a.x, b.x) && fuzzyEquals(a.y, b.y)
    }
}
