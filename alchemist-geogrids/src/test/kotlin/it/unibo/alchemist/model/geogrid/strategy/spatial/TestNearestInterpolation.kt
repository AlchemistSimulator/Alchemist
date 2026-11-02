/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy.spatial

import it.unibo.alchemist.model.geogrid.axes.ImmutableAxis
import it.unibo.alchemist.model.geogrid.strategy.spatial.NearestInterpolation.Companion.nearestIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestNearestInterpolation {

    // indices 0..3
    private val axis = ImmutableAxis(doubleArrayOf(10.0, 20.0, 30.0, 40.0))
    private val singleNode = ImmutableAxis(doubleArrayOf(42.0))

    @Test
    fun `returns the exact index on a node hit`() {
        assertEquals(0, axis.nearestIndex(10.0))
        assertEquals(1, axis.nearestIndex(20.0))
        assertEquals(3, axis.nearestIndex(40.0))
    }

    @Test
    fun `picks the closer node between two nodes`() {
        // closer to 20
        assertEquals(1, axis.nearestIndex(22.0))
        // closer to 30
        assertEquals(2, axis.nearestIndex(28.0))
    }

    @Test
    fun `resolves an exact tie to the lower index`() {
        assertEquals(1, axis.nearestIndex(25.0))
    }

    @Test
    fun `clamps below the first node`() {
        assertEquals(0, axis.nearestIndex(5.0))
        assertEquals(0, axis.nearestIndex(-1000.0))
    }

    @Test
    fun `clamps above the last node`() {
        assertEquals(3, axis.nearestIndex(50.0))
        assertEquals(3, axis.nearestIndex(1000.0))
    }

    @Test
    fun `always returns 0 on a single-node axis`() {
        assertEquals(0, singleNode.nearestIndex(42.0))
        assertEquals(0, singleNode.nearestIndex(0.0))
        assertEquals(0, singleNode.nearestIndex(100.0))
    }

    @Test
    fun `rejects a NaN query`() {
        assertFailsWith<IllegalArgumentException> { axis.nearestIndex(Double.NaN) }
    }
}
