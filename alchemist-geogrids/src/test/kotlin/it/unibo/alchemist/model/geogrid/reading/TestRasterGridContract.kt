/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.reading

import it.unibo.alchemist.mockGeoPosition
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Cartesian product of [first] and [second], in row-major order.
 */
private fun <A, B> cartesian(first: Iterable<A>, second: Iterable<B>): List<Pair<A, B>> =
    first.flatMap { a -> second.map { b -> a to b } }

/**
 * Returns from [start] to [stop] in [steps] intervals.
 */
private fun linspace(start: Double, stop: Double, steps: Int): List<Double> = (0..steps).map { i ->
    val t = i.toDouble() / steps
    (1 - t) * start + t * stop
}

/**
 * Contract shared by every [RasterGrid] implementation, verified
 * once and parameterized by the factory of the concrete implementation under test.
 */
abstract class RasterGridContract {

    /**
     * Factory of the concrete [RasterGrid] implementation under test.
     */
    protected abstract val gridOf: (DoubleArray, DoubleArray, DoubleArray) -> RasterGrid

    /**
     * 3 latitudes by 4 longitudes.
     * The cell value at (iLat, iLon) is iLat * 10 + iLon,
     * so it is easily verifiable.
     */
    private val lats = doubleArrayOf(10.0, 20.0, 30.0)
    private val lons = doubleArrayOf(5.0, 15.0, 25.0, 35.0)
    private val values = DoubleArray(12) { idx -> idx / lons.size * 10.0 + (idx % lons.size) }
    private val grid: RasterGrid by lazy { gridOf(lats, lons, values) }

    // Value access tests
    @Test
    fun `valueAt should return the correct value for an interior cell`() {
        // (iLat=1, iLong=2), then index = 1*4 + 2 = 6, then value = 1*10 + 2 = 12
        assertEquals(12.0, grid.valueAt(1, 2))
    }

    @Test
    fun `valueAt should return correct values at all four corners`() {
        assertEquals(0.0, grid.valueAt(0, 0)) // bottom left corner
        assertEquals(3.0, grid.valueAt(0, 3)) // bottom right
        assertEquals(20.0, grid.valueAt(2, 0)) // top left
        assertEquals(23.0, grid.valueAt(2, 3)) // top right
    }

    // Spatial coverage check
    @Test
    fun `isInBounds should return true for every position within the spatial extent`() {
        val steps = 40
        cartesian(
            linspace(lats.first(), lats.last(), steps),
            linspace(lons.first(), lons.last(), steps),
        ).forEach { (lat, lon) ->
            assertTrue(grid.isInBounds(mockGeoPosition(lat, lon)), "($lat, $lon) should be in bounds")
        }
    }

    @Test
    fun `isInBounds should return false when a coordinate falls outside the spatial extent`() {
        val offset = 0.5
        val midLat = lats[lats.size / 2]
        val midLon = lons[lons.size / 2]
        listOf(
            lats.first() - offset to midLon,
            lats.last() + offset to midLon,
            midLat to lons.first() - offset,
            midLat to lons.last() + offset,
        ).forEach { (lat, lon) ->
            assertFalse(grid.isInBounds(mockGeoPosition(lat, lon)), "($lat, $lon) should be out of bounds")
        }
    }

    // Missing values tests
    @Test
    fun `valueAt should return NaN for missing values`() {
        // (iLat=1, iLon=1) is a missing value
        val nanValues = DoubleArray(12) { idx -> if (idx == 5) Double.NaN else idx.toDouble() }
        val nullGrid = ArrayRasterGrid(lats, lons, nanValues)
        val value = nullGrid.valueAt(1, 1)
        assertTrue(value.isNaN(), "Expected NaN, but got $value")
    }

    @Test
    fun `a grid entirely made of NaN should return NaN everywhere`() {
        val allNan = ArrayRasterGrid(lats, lons, DoubleArray(12) { Double.NaN })
        cartesian(lats.indices, lons.indices).forEach { (iLat, iLon) ->
            val value = allNan.valueAt(iLat, iLon)
            assertTrue(value.isNaN(), "Expected NaN at ($iLat, $iLon), but got $value")
        }
    }

    // Axis tests
    @Test
    fun `latitudes should be accessible and match the constructor argument`() {
        assertContentEquals(lats, grid.latitudes)
    }

    @Test
    fun `longitudes should be accessible and match the constructor argument`() {
        assertContentEquals(lons, grid.longitudes)
    }

    // Dimension mismatch
    @Test
    fun `a mismatch between the number of cells and the number of values should raise an exception`() {
        assertFailsWith<IllegalArgumentException> {
            gridOf(
                lats,
                lons,
                DoubleArray(lats.size * lons.size - 1) { 0.0 },
            )
        }
    }

    @Test
    fun `axes that are not strictly increasing should raise an exception`() {
        assertFailsWith<IllegalArgumentException> {
            gridOf(
                lats.reversedArray(),
                lons,
                values,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            gridOf(
                lats,
                lons.reversedArray(),
                values,
            )
        }
    }
}

// tests the array implementation (suitable for dense grids)
class TestArrayRasterGrid : RasterGridContract() {
    override val gridOf: (DoubleArray, DoubleArray, DoubleArray) -> RasterGrid = ::ArrayRasterGrid
}

// tests the map implementation (suitable for sparse grids)
class TestMapRasterGrid : RasterGridContract() {
    override val gridOf: (DoubleArray, DoubleArray, DoubleArray) -> RasterGrid = ::MapRasterGrid
}
