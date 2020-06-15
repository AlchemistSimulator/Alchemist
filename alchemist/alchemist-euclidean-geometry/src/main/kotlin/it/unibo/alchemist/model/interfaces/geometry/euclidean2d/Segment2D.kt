package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.rangeFromUnordered
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
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
    val length: Double

    /**
     * Indicates if the two endpoints coincide (= segment has zero length).
     */
    val isDegenerate: Boolean

    /**
     * Indicates if the segment is aligned to the x-axis, this is true if [isDegenerate].
     */
    val isHorizontal: Boolean

    /**
     * Indicates if the segment is aligned to the y-axis, this is true if [isDegenerate].
     */
    val isVertical: Boolean

    /**
     * The medium point of the segment.
     */
    val midPoint: P

    /**
     * The vector representing the movement from [first] to [second].
     */
    val toVector: P

    /**
     * @returns the [Line2D] passing through [first] and [second]. Throws an [UnsupportedOperationException] if the
     * segment [isDegenerate].
     */
    fun toLine(): Line2D<P>

    /**
     * Creates a copy of this Segment2D using the specified [first] and [second] points.
     */
    fun copyWith(first: P = this.first, second: P = this.second): Segment2D<P>

    /**
     * Checks if the segment contains a [point].
     */
    fun contains(point: P): Boolean

    /**
     * Finds the point of the segment which is closest to the provided [point].
     */
    fun closestPointTo(point: P): P

    /**
     * Computes the shortest distance between the segment and the given [point].
     */
    @JvmDefault
    fun distanceTo(point: P): Double = closestPointTo(point).distanceTo(point)

    /**
     * Computes the shortest distance between two segments (= the shortest distance between any two of their points).
     */
    fun distanceTo(other: Segment2D<P>): Double

    /**
     * Checks if two segments are parallel. Throws an [UnsupportedOperationException] if any of the two segment
     * [isDegenerate].
     */
    fun isParallelTo(other: Segment2D<P>): Boolean

    /**
     * Checks if [first], [second] and [point] lie on a single line.
     */
    fun isCollinearWith(point: P): Boolean

    /**
     * Checks if two segments lie on a single line.
     */
    fun isCollinearWith(other: Segment2D<P>): Boolean

    /**
     * Checks if two segments overlap (= are collinear and share one or more points).
     */
    fun overlapsWith(other: Segment2D<P>): Boolean

    /**
     * Intersects two segments.
     */
    fun intersect(other: Segment2D<P>): Intersection2D<P>

    /**
     * Intersects a segment and a circle.
     */
    fun intersectCircle(center: P, radius: Double): Intersection2D<P>

    /**
     * @returns a shrunk version of the segment, [factor] is a percentage in [0, 0.5] indicating how much
     * the segment should be reduced on each size.
     */
    @JvmDefault
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
    @JvmDefault
    fun isInRectangle(origin: Vector2D<*>, width: Double, height: Double) =
        first.isInRectangle(origin, width, height) && second.isInRectangle(origin, width, height)

    /**
     * Maps the segment a [ClosedRange], this is done by extracting either the X coordinates or
     * the Y coordinates of the two endpoints of the segment. [getXCoords] indicates which pair
     * of coordinates should be extracted (defaults to [isHorizontal]).
     * This can be useful e.g. to represent portions of axis-aligned segments without creating
     * new ones.
     */
    @JvmDefault
    fun toRange(getXCoords: Boolean = this.isHorizontal): ClosedRange<Double> = when {
        getXCoords -> rangeFromUnordered(first.x, second.x)
        else -> rangeFromUnordered(first.y, second.y)
    }
}
