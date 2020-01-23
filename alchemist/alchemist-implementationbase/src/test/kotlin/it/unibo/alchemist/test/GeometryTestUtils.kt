package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.geometry.IntersectionResultTypes
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShapeFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal fun Euclidean2DShapeFactory.oneOfEachWithSize(size: Double) =
    mapOf(
        "circle" to circle(size * 2),
        "circleSector" to circleSector(size * 2, Math.PI, 0.0),
        "rectangle" to rectangle(size, size),
        "adimensional" to adimensional()
    )

internal const val DEFAULT_SHAPE_SIZE: Double = 1.0

class TestGeometryUtils {

    @Test
    fun testIntersection() {
        var s1 = Pair(Euclidean2DPosition(1.0, 1.0), Euclidean2DPosition(5.0, 5.0)) // (1,1) -> (5,5)
        var s2 = Pair(Euclidean2DPosition(3.0, 1.0), Euclidean2DPosition(1.0, 3.0)) // (3,1) -> (1,3)
        var i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.INTERSECTING, i.type)
        Assertions.assertEquals(Euclidean2DPosition(2.0, 2.0), i.intersection.get())
        // share an endpoint
        s2 = Pair(Euclidean2DPosition(5.0, 5.0), Euclidean2DPosition(6.0, 1.0)) // (5,5) -> (6,1)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.INTERSECTING, i.type)
        Assertions.assertEquals(Euclidean2DPosition(5.0, 5.0), i.intersection.get())
        // share an endpoint and collinear
        s2 = Pair(Euclidean2DPosition(5.0, 5.0), Euclidean2DPosition(6.0, 6.0)) // (5,5) -> (6,6)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.INTERSECTING, i.type)
        Assertions.assertEquals(Euclidean2DPosition(5.0, 5.0), i.intersection.get())
        // parallel
        s2 = Pair(Euclidean2DPosition(1.0, 2.0), Euclidean2DPosition(5.0, 6.0)) // (1,2) -> (5,6)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.NOT_INTERSECTING, i.type)
        // not parallel but not intersecting
        s2 = Pair(Euclidean2DPosition(2.0, 3.0), Euclidean2DPosition(1.0, 5.0)) // (2,3) -> (1,5)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.NOT_INTERSECTING, i.type)
        // collinear but disjoint
        s2 = Pair(Euclidean2DPosition(6.0, 6.0), Euclidean2DPosition(7.0, 7.0)) // (6,6) -> (7,7)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_DISJOINT, i.type)
        // coincident
        i = intersection(s1, s1)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_OVERLAPPING, i.type)
        // overlapping 1
        s2 = Pair(Euclidean2DPosition(3.0, 3.0), Euclidean2DPosition(7.0, 7.0)) // (3,3) -> (7,7)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_OVERLAPPING, i.type)
        // overlapping 2
        s2 = Pair(Euclidean2DPosition(-3.0, -3.0), Euclidean2DPosition(4.0, 4.0)) // (-3,-3) -> (4,4)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_OVERLAPPING, i.type)
        // one contains the other
        s2 = Pair(Euclidean2DPosition(-3.0, -3.0), Euclidean2DPosition(7.0, 7.0)) // (-3,-3) -> (7,7)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_OVERLAPPING, i.type)
        // share an endpoint but overlapping
        s2 = Pair(Euclidean2DPosition(3.0, 3.0), Euclidean2DPosition(5.0, 5.0)) // (3,3) -> (5,5)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_OVERLAPPING, i.type)
        // axis aligned tests
        // intersection
        s1 = Pair(Euclidean2DPosition(1.0, 1.0), Euclidean2DPosition(5.0, 1.0)) // (1,1) -> (5,1)
        s2 = Pair(Euclidean2DPosition(3.0, -1.0), Euclidean2DPosition(3.0, 1.0)) // (3,-1) -> (3,1)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.INTERSECTING, i.type)
        Assertions.assertEquals(Euclidean2DPosition(3.0, 1.0), i.intersection.get())
        s2 = Pair(Euclidean2DPosition(3.0, -1.0), Euclidean2DPosition(3.0, 5.0)) // (3,-1) -> (3,5)
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.INTERSECTING, i.type)
        Assertions.assertEquals(Euclidean2DPosition(3.0, 1.0), i.intersection.get())
        s2 = Pair(Euclidean2DPosition(5.0, 1.0), Euclidean2DPosition(6.0, 1.0))
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.INTERSECTING, i.type)
        Assertions.assertEquals(Euclidean2DPosition(5.0, 1.0), i.intersection.get())
        // collinearity x-axis
        s2 = Pair(Euclidean2DPosition(4.9, 1.0), Euclidean2DPosition(6.0, 1.0))
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_OVERLAPPING, i.type)
        s2 = Pair(Euclidean2DPosition(6.0, 1.0), Euclidean2DPosition(7.0, 1.0))
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_DISJOINT, i.type)
        // collinearity y-axis
        s1 = Pair(Euclidean2DPosition(1.0, 1.0), Euclidean2DPosition(1.0, 6.0))
        s2 = Pair(Euclidean2DPosition(1.0, 1.0), Euclidean2DPosition(1.0, -6.0))
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.INTERSECTING, i.type)
        Assertions.assertEquals(Euclidean2DPosition(1.0, 1.0), i.intersection.get())
        s2 = Pair(Euclidean2DPosition(1.0, -1.0), Euclidean2DPosition(1.0, -6.0))
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_DISJOINT, i.type)
        s2 = Pair(Euclidean2DPosition(1.0, 2.0), Euclidean2DPosition(1.0, -6.0))
        i = intersection(s1, s2)
        Assertions.assertEquals(IntersectionResultTypes.COLLINEAR_OVERLAPPING, i.type)
    }
}