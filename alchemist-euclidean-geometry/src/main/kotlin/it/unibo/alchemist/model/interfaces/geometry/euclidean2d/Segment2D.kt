package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.fuzzyIn
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.SlopeInterceptLine2D
import it.unibo.alchemist.rangeFromUnordered
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.lang.UnsupportedOperationException

/**
 * Defines a line segment in a cartesian plane, endpoints are included.
 */
interface Segment2D<P : Vector2D<P>> {

    /**
     * The first endpoint of the segment.
     */
    val first: P

    /**
     * The second endpoint of the segment.
     */
    val second: P

    /**
     * The length of the segment.
     */
    val length: Double get() = toVector.magnitude

    /**
     * Indicates if the two endpoints coincide (= segment has zero length).
     */
    val isDegenerate: Boolean get() = fuzzyEquals(first, second)

    /**
     * Indicates if the segment is aligned to the x-axis, this is true if [isDegenerate].
     */
    val isHorizontal: Boolean get() = fuzzyEquals(first.y, second.y)

    /**
     * Indicates if the segment is aligned to the y-axis, this is true if [isDegenerate].
     */
    val isVertical: Boolean get() = fuzzyEquals(first.x, second.x)

    /**
     * The medium point of the segment.
     */
    val midPoint: P get() = first.newFrom((first.x + second.x) / 2, (first.y + second.y) / 2)

    /**
     * The vector representing the movement from [first] to [second].
     */
    val toVector: P get() = second - first

    /**
     * @returns the [Line2D] passing through [first] and [second]. Throws an [UnsupportedOperationException] if the
     * segment [isDegenerate].
     */
    fun toLine(): Line2D<P> = when {
        isDegenerate -> throw UnsupportedOperationException("degenerate segment can't be converted to line")
        else -> SlopeInterceptLine2D.fromSegment(this)
    }

    /**
     * Creates a copy of this Segment2D using the specified [first] and [second] points.
     */
    fun copyWith(first: P = this.first, second: P = this.second): Segment2D<P>

    /**
     * Checks if the segment contains a [point].
     */
    fun contains(point: P): Boolean = isCollinearWith(point) &&
        point.x fuzzyIn rangeFromUnordered(first.x, second.x) &&
        point.y fuzzyIn rangeFromUnordered(first.y, second.y)

    /**
     * Finds the point of the segment which is closest to the provided [point].
     */
    fun closestPointTo(point: P): P = when {
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
     * Computes the shortest distance between the segment and the given [point].
     */
    fun distanceTo(point: P): Double = closestPointTo(point).distanceTo(point)

    /**
     * Computes the shortest distance between two segments (= the shortest distance between any two of their points).
     */
    fun distanceTo(other: Segment2D<P>): Double = when {
        intersect(other) !is Intersection2D.None -> 0.0
        else -> listOf(
            distanceTo(other.first),
            distanceTo(other.second),
            other.distanceTo(first),
            other.distanceTo(second)
        ).minOrNull() ?: Double.POSITIVE_INFINITY
    }

    /**
     * Checks if two segments are parallel. Throws an [UnsupportedOperationException] if any of the two segment
     * [isDegenerate].
     */
    fun isParallelTo(other: Segment2D<P>): Boolean = when {
        isDegenerate || other.isDegenerate ->
            throw UnsupportedOperationException("parallelism check is meaningless for degenerate segments")
        else -> toLine().isParallelTo(other.toLine())
    }

    /**
     * Checks if [first], [second] and [point] lie on a single line.
     */
    fun isCollinearWith(point: P): Boolean = isDegenerate || toLine().contains(point)

    /**
     * Checks if two segments lie on a single line.
     */
    fun isCollinearWith(other: Segment2D<P>): Boolean = when {
        isDegenerate -> other.isCollinearWith(first)
        else -> isCollinearWith(other.first) && isCollinearWith(other.second)
    }

    /**
     * Checks if two segments overlap (= are collinear and share one or more points).
     */
    fun overlapsWith(other: Segment2D<P>): Boolean = isCollinearWith(other) &&
        (contains(other.first) || contains(other.second) || other.contains(first) || other.contains(second))

    /**
     * Intersects two segments.
     */
    fun intersect(other: Segment2D<P>): Intersection2D<P> = when {
        isDegenerate ->
            Intersection2D.None.takeUnless { other.contains(first) } ?: Intersection2D.SinglePoint(first)
        other.isDegenerate ->
            other.intersect(this)
        isCollinearWith(other) && overlapsWith(other) ->
            endpointSharedWith(other)
                ?.let { Intersection2D.SinglePoint(it) }
                /*
                 * Overlapping and sharing more than one point means that
                 * they share a portion of segment (= infinite points).
                 */
                ?: Intersection2D.InfinitePoints
        else ->
            Intersection2D.create(
                toLine().intersect(other.toLine()).asList.filter { contains(it) && other.contains(it) }
            )
    }

    /**
     * Intersects a segment and a circle.
     */
    fun intersectCircle(center: P, radius: Double): Intersection2D<P> = when {
        isDegenerate ->
            Intersection2D.None
                .takeUnless { fuzzyEquals(first.distanceTo(center), radius) }
                ?: Intersection2D.SinglePoint(first)
        else ->
            Intersection2D.create(toLine().intersectCircle(center, radius).asList.filter { contains(it) })
    }

    /**
     * @returns a shrunk version of the segment, [factor] is a percentage in [0, 0.5] indicating how much
     * the segment should be reduced on each size.
     */
    fun shrunk(factor: Double): Segment2D<P> = when (factor) {
        !in 0.0..0.5 -> throw IllegalArgumentException("$factor not in [0, 0.5]")
        else -> copyWith(
            first = first + (second - first).resized(factor * length),
            second = second + (first - second).resized(factor * length)
        )
    }

    /**
     * Checks if this segment is inside a rectangular region described by an [origin], [width] and
     * [height] (must be positive).
     */
    fun isInRectangle(origin: Vector2D<*>, width: Double, height: Double) =
        first.isInRectangle(origin, width, height) && second.isInRectangle(origin, width, height)

    /**
     * Maps the segment a [ClosedRange], this is done by extracting either the X coordinates or
     * the Y coordinates of the two endpoints of the segment. [getXCoords] indicates which pair
     * of coordinates should be extracted (defaults to [isHorizontal]).
     * This can be useful e.g. to represent portions of axis-aligned segments without creating
     * new ones.
     */
    fun toRange(getXCoords: Boolean = this.isHorizontal): ClosedRange<Double> = when {
        getXCoords -> rangeFromUnordered(first.x, second.x)
        else -> rangeFromUnordered(first.y, second.y)
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
        private fun <P : Vector2D<P>> fuzzyEquals(a: P, b: P): Boolean = fuzzyEquals(a.x, b.x) && fuzzyEquals(a.y, b.y)
    }
}
