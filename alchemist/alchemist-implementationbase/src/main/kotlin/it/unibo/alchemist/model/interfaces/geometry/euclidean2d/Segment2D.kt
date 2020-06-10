package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.implementations.geometry.fuzzyIn
import it.unibo.alchemist.model.implementations.geometry.rangeFromUnordered
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import org.danilopianini.lang.MathUtils
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.lang.UnsupportedOperationException
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Defines a segment from [first] to [second] in euclidean bidimensional space.
 */
interface Segment2D<P : Vector2D<P>> {

    /**
     * First point.
     */
    val first: P

    /**
     * Second point.
     */
    val second: P

    /**
     * Length of the segment.
     */
    @JvmDefault
    val length: Double get() = toVector().magnitude

    /**
     * A segment is degenerate if its points coincide.
     */
    @JvmDefault
    val isDegenerate: Boolean get() = fuzzyEquals(first.x, second.x) && fuzzyEquals(first.y, second.y)

    /**
     * Indicates if the segment is aligned to the x axis.
     */
    @JvmDefault
    val isHorizontal: Boolean get() = fuzzyEquals(first.y, second.y)

    /**
     * Indicates if the segment is aligned to the y axis.
     */
    @JvmDefault
    val isVertical: Boolean get() = fuzzyEquals(first.x, second.x)

    /**
     * Indicates if the segment is axis-aligned.
     */
    @JvmDefault
    val isAlignedToAnyAxis: Boolean get() = isHorizontal || isVertical

    /**
     * The medium point of the segment.
     */
    @JvmDefault
    val midPoint: P get() = first.newFrom((first.x + second.x) / 2, (first.y + second.y) / 2)

    /**
     * The slope of the segment. If the segment [isDegenerate], this is [Double.NaN]. If the segment
     * [isVertical], this may be either [Double.POSITIVE_INFINITY] or [Double.NEGATIVE_INFINITY].
     */
    @JvmDefault
    val slope: Double get() = Double.NaN.takeIf { isDegenerate } ?: toVector().run { y / x }

    /**
     * The intercept of the line passing through [first] and [second]. If the segment [isDegenerate],
     * this is [Double.NaN]. If the segment [isVertical], this may be either [Double.POSITIVE_INFINITY]
     * or [Double.NEGATIVE_INFINITY].
     */
    @JvmDefault
    val intercept: Double get() = Double.NaN.takeIf { isDegenerate } ?: first.y - slope * first.x

    /**
     * Creates a copy of this Segment2D using the specified [first] and [second] points.
     */
    @JvmDefault
    fun copyWith(first: P = this.first, second: P = this.second): Segment2D<P>

    /**
     * @returns the vector representing the movement from [first] to [second].
     */
    @JvmDefault
    fun toVector(): P = second - first

    /**
     * Checks if the segment contains a [point]. Doubles are not directly compared, [MathUtils.fuzzyEquals]
     * is used instead.
     */
    @JvmDefault
    fun contains(point: P): Boolean = isCollinearWith(point) &&
        point.x fuzzyIn rangeFromUnordered(first.x, second.x) &&
        point.y fuzzyIn rangeFromUnordered(first.y, second.y)

    /**
     * Finds the point of the segment which is closest to the provided [point].
     */
    @JvmDefault
    fun closestPointTo(point: P): P = when {
        isDegenerate -> first
        contains(point) -> point
        else -> {
            /*
             * Intersection between the line defined by the segment and the line perpendicular to the segment
             * passing through the given point.
             */
            val intersection = intersectAsLines(copyWith(point, point + toVector().normal()))
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
     * Computes the distance between the segment and the given [point].
     */
    @JvmDefault
    fun distanceTo(point: P): Double = closestPointTo(point).distanceTo(point)

    /**
     * Computes the (minimum) distance between two segments.
     */
    @JvmDefault
    fun distanceTo(other: Segment2D<P>): Double =
        listOf(distanceTo(other.first), distanceTo(other.second), other.distanceTo(first), other.distanceTo(second))
            .min() ?: Double.POSITIVE_INFINITY

    /**
     * Checks if the segment is parallel to [other]. Segments must not be degenerate.
     */
    @JvmDefault
    fun isParallelTo(other: Segment2D<P>): Boolean {
        require(!isDegenerate && !other.isDegenerate) { "parallelism is meaningless for degenerate segments" }
        return (isVertical && other.isVertical) || fuzzyEquals(slope, other.slope)
    }

    /**
     * Checks if [first], [second] and [point] lie on a single line.
     */
    @JvmDefault
    fun isCollinearWith(point: P): Boolean = when {
        isDegenerate -> true
        isVertical -> fuzzyEquals(first.x, point.x)
        isHorizontal -> fuzzyEquals(first.y, point.y)
        else -> fuzzyEquals(computeY(point.x), point.y)
    }

    /**
     * Checks if two segments lie on a single line.
     */
    @JvmDefault
    fun isCollinearWith(other: Segment2D<P>): Boolean = when {
        other.isDegenerate -> isCollinearWith(other.first)
        isDegenerate -> other.isCollinearWith(first)
        !isParallelTo(other) -> false
        isVertical && isVertical -> fuzzyEquals(first.x, other.first.x)
        else -> fuzzyEquals(intercept, other.intercept)
    }

    /**
     * @returns true if the segments are collinear and share one or more points.
     */
    fun overlapsWith(other: Segment2D<P>): Boolean = isCollinearWith(other) &&
        (contains(other.first) || contains(other.second) || other.contains(first) || other.contains(second))

    /**
     * Finds the intersection of two lines represented by segments. Segments must not be degenerate.
     */
    @JvmDefault
    fun intersectAsLines(other: Segment2D<P>): Intersection2D<P> = when {
        isDegenerate || other.isDegenerate -> throw UnsupportedOperationException("degenerate segments are not lines")
        isCollinearWith(other) -> Intersection2D.InfinitePoints
        /*
         * But not collinear.
         */
        isParallelTo(other) -> Intersection2D.None
        else -> {
            val intersection = when {
                isVertical -> first.newFrom(first.x, other.computeY(first.x))
                other.isVertical -> first.newFrom(other.first.x, computeY(other.first.x))
                else -> {
                    val x = (other.intercept - intercept) / (slope - other.slope)
                    first.newFrom(x, computeY(x))
                }
            }
            Intersection2D.SinglePoint(intersection)
        }
    }

    /**
     * Finds the intersection of two segments.
     */
    @JvmDefault
    fun intersectSegment(other: Segment2D<P>): Intersection2D<P> = when {
        isDegenerate -> Intersection2D.SinglePoint(first).takeIf { other.contains(first) } ?: Intersection2D.None
        other.isDegenerate -> other.intersectSegment(this)
        else -> intersectAsLines(other).let { intersection ->
            when {
                intersection is Intersection2D.SinglePoint && contains(intersection.point)
                    && other.contains(intersection.point) -> intersection
                intersection is Intersection2D.InfinitePoints && overlapsWith(other) -> endpointSharedWith(other)
                    ?.let { Intersection2D.SinglePoint(it) }
                    ?: Intersection2D.InfinitePoints
                else -> Intersection2D.None
            }
        }
    }

    /**
     * Finds the intersection between the segment and a circle.
     */
    fun intersectCircle(center: P, radius: Double): Intersection2D<P> {
        fun <P : Vector2D<P>> intersectionPoint(segment: Segment2D<P>, vector: Vector2D<P>, t: Double): P? =
            t.takeIf { it fuzzyIn 0.0..1.0 }?.let {
                val x = segment.first.x + t * vector.x
                val y = segment.first.y + t * vector.y
                segment.first.newFrom(x, y)
            }
        val vector = toVector()
        /*
         * a, b and c are the terms of the 2nd grade equation of the intersection
         */
        val a = vector.x.pow(2) + vector.y.pow(2)
        val b = 2 * (vector.x * (first.x - center.x) + vector.y * (first.y - center.y))
        val c = (first.x - center.x).pow(2) + (first.y - center.y).pow(2) - radius.pow(2)
        val det = b.pow(2) - 4 * a * c
        return when {
            fuzzyEquals(a, 0.0) || a < 0.0 || det < 0.0 -> Intersection2D.None
            fuzzyEquals(det, 0.0) -> {
                val t = -b / (2 * a)
                when (val p = intersectionPoint(this, vector, t)) {
                    null -> Intersection2D.None
                    else -> Intersection2D.SinglePoint(p)
                }
            }
            else -> {
                val t1 = (-b + sqrt(det)) / (2 * a)
                val t2 = (-b - sqrt(det)) / (2 * a)
                val p1 = intersectionPoint(this, vector, t1)
                val p2 = intersectionPoint(this, vector, t2)
                Intersection2D.create(listOfNotNull(p1, p2))
            }
        }
    }

    /**
     * Checks whether this segment is inside a rectangular region described by an [origin], [width] and
     * [height] (must be positive).
     */
    @JvmDefault
    fun isInRectangle(origin: Vector2D<*>, width: Double, height: Double) =
        first.isInRectangle(origin, width, height) && second.isInRectangle(origin, width, height)

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
     * Maps the segment a [ClosedRange], this is done by extracting either the X coordinates or
     * the Y coordinates of the two endpoints of the segment. [getXCoords] indicates which pair
     * of coordinates should be extracted.
     * This can be useful e.g. to represent portions of axis-aligned segments without creating
     * new ones.
     */
    @JvmDefault
    fun toRange(getXCoords: Boolean = this.isHorizontal): ClosedRange<Double> = when {
        getXCoords -> rangeFromUnordered(first.x, second.x)
        else -> rangeFromUnordered(first.y, second.y)
    }

    private fun computeY(x: Double): Double = when {
        isVertical -> throw UnsupportedOperationException("vertical line")
        isHorizontal -> first.y
        else -> slope * x + intercept
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
         * TODO(move to Vector interface)
         */
        private fun <P : Vector2D<P>> fuzzyEquals(a: P, b: P): Boolean = fuzzyEquals(a.x, b.x) && fuzzyEquals(a.y, b.y)
    }
}
