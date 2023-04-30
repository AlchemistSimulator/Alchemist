/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import it.unibo.alchemist.model.geometry.AwtMutableConvexPolygon
import it.unibo.alchemist.model.geometry.Segment2DImpl
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests the creation of [AwtMutableConvexPolygon]s, the addition, removal and movement of vertices,
 * and the replacement of edges.
 */
class TestAwtMutableConvexPolygon {

    lateinit var polygon: AwtMutableConvexPolygon

    @BeforeEach
    fun setUp() {
        polygon = AwtMutableConvexPolygon(
            mutableListOf(
                Euclidean2DPosition(0.0, 0.0),
                Euclidean2DPosition(1.0, 0.0),
                Euclidean2DPosition(1.0, 1.0),
                Euclidean2DPosition(0.0, 1.0),
            ),
        )
    }

    @Test
    fun testCreation() {
        shouldNotThrow<IllegalArgumentException> {
            AwtMutableConvexPolygon(
                mutableListOf(
                    Euclidean2DPosition(0.0, 0.0),
                    Euclidean2DPosition(1.0, 0.0),
                    Euclidean2DPosition(1.0, 1.0),
                    Euclidean2DPosition(0.0, 1.0),
                ),
            )
        }
        shouldThrow<IllegalArgumentException> {
            AwtMutableConvexPolygon(
                mutableListOf(
                    Euclidean2DPosition(0.0, 0.0),
                    Euclidean2DPosition(0.0, 6.0),
                    Euclidean2DPosition(3.0, 6.0),
                    Euclidean2DPosition(3.0, 3.0),
                    Euclidean2DPosition(-1.0, 3.0),
                    Euclidean2DPosition(-1.0, 8.0),
                    Euclidean2DPosition(6.0, 8.0),
                    Euclidean2DPosition(6.0, 0.0),
                ),
            )
        }
        shouldNotThrow<IllegalArgumentException> {
            AwtMutableConvexPolygon(
                mutableListOf(
                    Euclidean2DPosition(0.0, 0.0),
                    Euclidean2DPosition(-1.0, 3.0),
                    Euclidean2DPosition(-1.0, 8.0),
                    Euclidean2DPosition(6.0, 8.0),
                    Euclidean2DPosition(6.0, 0.0),
                ),
            )
        }
    }

    @Test
    fun testAddVertex() {
        val p = AwtMutableConvexPolygon(
            mutableListOf(
                Euclidean2DPosition(0.0, 0.0),
                Euclidean2DPosition(1.0, 0.0),
                Euclidean2DPosition(1.0, 1.0),
                Euclidean2DPosition(0.0, 1.0),
            ),
        )
        assertTrue(p.addVertex(2, 1.5, 0.5))
        assertTrue(p.removeVertex(2))
        assertFalse(p.addVertex(2, 0.5, 0.5))
    }

    @Test
    fun testRemoveVertex() {
        assertTrue(polygon.removeVertex(2))
        assertFalse(polygon.removeVertex(2))
        assertTrue(polygon.addVertex(0, 0.0, 0.0))
        assertTrue(polygon.addVertex(0, 0.0, 0.0))
        assertFalse(polygon.removeVertex(4))
        assertTrue(polygon.removeVertex(2))
        assertTrue(polygon.removeVertex(1))
        assertFalse(polygon.removeVertex(0))
    }

    @Test
    fun testMoveVertex() {
        assertTrue(polygon.moveVertex(2, 3.0, 3.0))
        assertFalse(polygon.moveVertex(2, -3.0, -3.0))
        assertFalse(polygon.moveVertex(1, 0.5, 1.5))
    }

    @Test
    fun testReplaceEdge() {
        var newEdge = Segment2DImpl(
            Euclidean2DPosition(1.5, -0.5),
            Euclidean2DPosition(1.5, 1.5),
        )
        assertTrue(polygon.replaceEdge(1, newEdge))
        newEdge = Segment2DImpl(
            Euclidean2DPosition(1.5, 1.5),
            Euclidean2DPosition(1.5, -0.5),
        )
        assertFalse(polygon.replaceEdge(1, newEdge))
        newEdge = Segment2DImpl(
            Euclidean2DPosition(0.5, 0.3),
            Euclidean2DPosition(0.5, 0.6),
        )
        assertTrue(polygon.replaceEdge(1, newEdge))
        newEdge = Segment2DImpl(
            Euclidean2DPosition(0.5, 0.3),
            Euclidean2DPosition(0.5, 0.6),
        )
        assertTrue(polygon.replaceEdge(1, newEdge))
        polygon = AwtMutableConvexPolygon(
            mutableListOf(
                Euclidean2DPosition(0.0, 0.0),
                Euclidean2DPosition(3.0, 0.0),
                Euclidean2DPosition(4.0, 1.0),
                Euclidean2DPosition(4.0, 2.0),
                Euclidean2DPosition(3.0, 3.0),
                Euclidean2DPosition(0.0, 3.0),
            ),
        )
        newEdge = Segment2DImpl(
            Euclidean2DPosition(-0.5, 1.0),
            Euclidean2DPosition(-0.5, 2.0),
        )
        assertFalse(polygon.replaceEdge(1, newEdge))
    }
}
