package it.unibo.alchemist.test

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.AwtMutableConvexPolygon
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.MutableConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.geom.Rectangle2D
import java.lang.IllegalArgumentException

class TestMutableConvexPolygon {

    @Test
    fun testCreation() {
        shouldNotThrow<IllegalArgumentException> {
            AwtMutableConvexPolygon(mutableListOf(
                Euclidean2DPosition(0.0, 0.0),
                Euclidean2DPosition(1.0, 0.0),
                Euclidean2DPosition(1.0, 1.0),
                Euclidean2DPosition(0.0, 1.0)
            ))
        }
        shouldThrow<IllegalArgumentException> {
            AwtMutableConvexPolygon(mutableListOf(
                Euclidean2DPosition(0.0, 0.0),
                Euclidean2DPosition(0.0, 6.0),
                Euclidean2DPosition(3.0, 6.0),
                Euclidean2DPosition(3.0, 3.0),
                Euclidean2DPosition(-1.0, 3.0),
                Euclidean2DPosition(-1.0, 8.0),
                Euclidean2DPosition(6.0, 8.0),
                Euclidean2DPosition(6.0, 0.0)
            ))
        }
        shouldNotThrow<IllegalArgumentException> {
            AwtMutableConvexPolygon(mutableListOf(
                Euclidean2DPosition(0.0, 0.0),
                Euclidean2DPosition(-1.0, 3.0),
                Euclidean2DPosition(-1.0, 8.0),
                Euclidean2DPosition(6.0, 8.0),
                Euclidean2DPosition(6.0, 0.0)
            ))
        }
    }

    @Test
    fun testAddVertex() {
        val p = AwtMutableConvexPolygon(mutableListOf(
            Euclidean2DPosition(0.0, 0.0),
            Euclidean2DPosition(1.0, 0.0),
            Euclidean2DPosition(1.0, 1.0),
            Euclidean2DPosition(0.0, 1.0)
        ))
        Assertions.assertEquals(true, p.addVertex(2, 1.5, 0.5))
        Assertions.assertEquals(true, p.removeVertex(2))
        Assertions.assertEquals(false, p.addVertex(2, 0.5, 0.5))
    }

    @Test
    fun testRemoveVertex() {
        val p = AwtMutableConvexPolygon(mutableListOf(
            Euclidean2DPosition(0.0, 0.0),
            Euclidean2DPosition(1.0, 0.0),
            Euclidean2DPosition(1.0, 1.0),
            Euclidean2DPosition(0.0, 1.0)
        ))
        Assertions.assertEquals(true, p.removeVertex(2))
        Assertions.assertEquals(false, p.removeVertex(2))
        Assertions.assertEquals(true, p.addVertex(0, 0.0, 0.0))
        Assertions.assertEquals(true, p.addVertex(0, 0.0, 0.0))
        Assertions.assertEquals(false, p.removeVertex(4))
        Assertions.assertEquals(true, p.removeVertex(2))
        Assertions.assertEquals(true, p.removeVertex(1))
        Assertions.assertEquals(false, p.removeVertex(0))
    }

    @Test
    fun testMoveVertex() {
        val p = AwtMutableConvexPolygon(mutableListOf(
            Euclidean2DPosition(0.0, 0.0),
            Euclidean2DPosition(1.0, 0.0),
            Euclidean2DPosition(1.0, 1.0),
            Euclidean2DPosition(0.0, 1.0)
        ))
        Assertions.assertEquals(true, p.moveVertex(2, 3.0, 3.0))
        Assertions.assertEquals(false, p.moveVertex(2, -3.0, -3.0))
        Assertions.assertEquals(false, p.moveVertex(1, 0.5, 1.5))
    }

    @Test
    fun testMoveEdge() {
        var p = AwtMutableConvexPolygon(mutableListOf(
            Euclidean2DPosition(0.0, 0.0),
            Euclidean2DPosition(1.0, 0.0),
            Euclidean2DPosition(1.0, 1.0),
            Euclidean2DPosition(0.0, 1.0)
        ))
        var newEdge = Segment2D(Euclidean2DPosition(1.5, -0.5), Euclidean2DPosition(1.5, 1.5))
        Assertions.assertEquals(true, p.replaceEdge(1, newEdge))
        newEdge = Segment2D(Euclidean2DPosition(1.5, 1.5), Euclidean2DPosition(1.5, -0.5))
        Assertions.assertEquals(false, p.replaceEdge(1, newEdge))
        newEdge = Segment2D(Euclidean2DPosition(0.5, 0.3), Euclidean2DPosition(0.5, 0.6))
        Assertions.assertEquals(true, p.replaceEdge(1, newEdge))
        newEdge = Segment2D(Euclidean2DPosition(0.5, 0.3), Euclidean2DPosition(0.5, 0.6))
        Assertions.assertEquals(true, p.replaceEdge(1, newEdge))
        p = AwtMutableConvexPolygon(mutableListOf(
            Euclidean2DPosition(0.0, 0.0),
            Euclidean2DPosition(3.0, 0.0),
            Euclidean2DPosition(4.0, 1.0),
            Euclidean2DPosition(4.0, 2.0),
            Euclidean2DPosition(3.0, 3.0),
            Euclidean2DPosition(0.0, 3.0)
        ))
        newEdge = Segment2D(Euclidean2DPosition(-0.5, 1.0), Euclidean2DPosition(-0.5, 2.0))
        Assertions.assertEquals(false, p.replaceEdge(1, newEdge))
    }
}
