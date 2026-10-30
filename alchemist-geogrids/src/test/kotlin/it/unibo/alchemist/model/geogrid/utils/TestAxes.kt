/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestAxes {

    // indices 0..3
    private val axis = doubleArrayOf(10.0, 20.0, 30.0, 40.0)
    private val singleNode = doubleArrayOf(42.0)

    // nearestIndex tests
    @Test
    fun `nearestIndex returns the exact index on a node hit`() {
        assertEquals(0, nearestIndex(axis, 10.0))
        assertEquals(1, nearestIndex(axis, 20.0))
        assertEquals(3, nearestIndex(axis, 40.0))
    }

    @Test
    fun `nearestIndex picks the closer node between two nodes`() {
        // closer to 20
        assertEquals(1, nearestIndex(axis, 22.0))
        // closer to 30
        assertEquals(2, nearestIndex(axis, 28.0))
    }

    @Test
    fun `nearestIndex resolves an exact tie to the lower index`() {
        assertEquals(1, nearestIndex(axis, 25.0))
    }

    @Test
    fun `nearestIndex clamps below the first node`() {
        assertEquals(0, nearestIndex(axis, 5.0))
        assertEquals(0, nearestIndex(axis, -1000.0))
    }

    @Test
    fun `nearestIndex clamps above the last node`() {
        assertEquals(3, nearestIndex(axis, 50.0))
        assertEquals(3, nearestIndex(axis, 1000.0))
    }

    @Test
    fun `nearestIndex always returns 0 on a single-node axis`() {
        assertEquals(0, nearestIndex(singleNode, 42.0))
        assertEquals(0, nearestIndex(singleNode, 0.0))
        assertEquals(0, nearestIndex(singleNode, 100.0))
    }

    @Test
    fun `nearestIndex rejects an empty axis`() {
        assertFailsWith<IllegalArgumentException> { nearestIndex(DoubleArray(0), 5.0) }
    }

    @Test
    fun `nearestIndex rejects a NaN query`() {
        assertFailsWith<IllegalArgumentException> { nearestIndex(axis, Double.NaN) }
    }

    // bracketIndices tests
    @Test
    fun `bracketIndices returns a degenerate pair on a node hit`() {
        assertEquals(2 to 2, bracketIndices(axis, 30.0))
    }

    @Test
    fun `bracketIndices brackets an interior coordinate`() {
        assertEquals(1 to 2, bracketIndices(axis, 23.0))
    }

    @Test
    fun `bracketIndices clamps outside the boundaries`() {
        assertEquals(0 to 0, bracketIndices(axis, 5.0))
        assertEquals(3 to 3, bracketIndices(axis, 50.0))
    }

    @Test
    fun `bracketIndices is degenerate on a single-node axis`() {
        assertEquals(0 to 0, bracketIndices(singleNode, 42.0))
        assertEquals(0 to 0, bracketIndices(singleNode, 0.0))
        assertEquals(0 to 0, bracketIndices(singleNode, 100.0))
    }

    @Test
    fun `bracketIndices rejects an empty axis and a NaN query`() {
        assertFailsWith<IllegalArgumentException> { bracketIndices(DoubleArray(0), 5.0) }
        assertFailsWith<IllegalArgumentException> { bracketIndices(axis, Double.NaN) }
    }

    // weight tests
    @Test
    fun `weight is 0 at the lower node and 1 at the upper node`() {
        assertEquals(0.0, weight(axis, 1, 2, 20.0))
        assertEquals(1.0, weight(axis, 1, 2, 30.0))
    }

    @Test
    fun `weight is the normalized position within the segment`() {
        // sets a tolerance for the results
        val tolerance = 1e-9
        assertEquals(0.5, weight(axis, 1, 2, 25.0), tolerance)
        assertEquals(0.3, weight(axis, 1, 2, 23.0), tolerance)
    }

    @Test
    fun `weight is 0 on a degenerate bracket`() {
        assertEquals(0.0, weight(axis, 2, 2, 30.0))
    }
}
