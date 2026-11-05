/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.spatialinterpolations

import it.unibo.alchemist.model.geogrid.axes.ImmutableAxis
import it.unibo.alchemist.model.geogrid.rastergrids.ArrayRasterGrid
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestSpatialInterpolation {

    private val nearest = NearestInterpolation
    private val bilinear = BilinearInterpolation

    private val tolerance = 1e-9

    /*
     * 2x2 grid.
     * It can be represented as follows:
     *           W(lon -100) E(lon 0)
     * N(lat 20)    20(NW)   100(NE)
     * S(lat 10)    0(SW)    10(SE)
     */
    private val southWest = 0.0
    private val southEast = 10.0
    private val northWest = 20.0
    private val northEast = 100.0
    private val grid = ArrayRasterGrid(
        latitudes = ImmutableAxis(doubleArrayOf(10.0, 20.0)),
        longitudes = ImmutableAxis(doubleArrayOf(-100.0, 0.0)),
        // row-major representation
        gridValues = doubleArrayOf(southWest, southEast, northWest, northEast),
    )

    /*
     * 3x3 grid used to verify correct cell selection in a multi-cell grid: on an affine field
     * bilinear reproduces the value exactly, so the expected result is just lat + lon + 200.
     */
    private val affineGrid = ArrayRasterGrid(
        latitudes = ImmutableAxis(doubleArrayOf(10.0, 20.0, 30.0)),
        longitudes = ImmutableAxis(doubleArrayOf(-100.0, 0.0, 100.0)),
        gridValues = doubleArrayOf(
            110.0, 210.0, 310.0, // lat = 10
            120.0, 220.0, 320.0, // lat = 20
            130.0, 230.0, 330.0, // lat = 30
        ),
    )

    // Nearest spatial interpolation strategy tests
    @Test
    fun `nearest returns the cell value on an exact node hit`() {
        assertEquals(southWest, nearest.valueAt(grid, LatLongPosition(10.0, -100.0)))
        assertEquals(northEast, nearest.valueAt(grid, LatLongPosition(20.0, 0.0)))
    }

    @Test
    fun `nearest picks the closest cell`() {
        // closest to south-west
        assertEquals(southWest, nearest.valueAt(grid, LatLongPosition(12.0, -90.0)))
        // closest to north-east
        assertEquals(northEast, nearest.valueAt(grid, LatLongPosition(18.0, -10.0)))
    }

    @Test
    fun `nearest resolves an exact tie to the lower index on each axis`() {
        // lat 15 and lon -50 are both exactly between nodes, so the lower index should be picked
        assertEquals(southWest, nearest.valueAt(grid, LatLongPosition(15.0, -50.0)))
    }

    @Test
    fun `nearest selects the correct cell in a multi-cell grid`() {
        // nearest node to (22, 90) is (20, 100)
        assertEquals(320.0, nearest.valueAt(affineGrid, LatLongPosition(22.0, 90.0)))
    }

    @Test
    fun `nearest clamps positions outside the grid to its closest edge`() {
        assertEquals(southWest, nearest.valueAt(grid, LatLongPosition(-80.0, -170.0)))
        assertEquals(northEast, nearest.valueAt(grid, LatLongPosition(80.0, 170.0)))
        assertEquals(southEast, nearest.valueAt(grid, LatLongPosition(0.0, 50.0)))
    }

    @Test
    fun `nearest always returns the only value of a single-node grid`() {
        val singleNode = ArrayRasterGrid(
            latitudes = ImmutableAxis(doubleArrayOf(42.0)),
            longitudes = ImmutableAxis(doubleArrayOf(12.0)),
            gridValues = doubleArrayOf(7.0),
        )
        listOf(42.0 to 12.0, 0.0 to 0.0, 80.0 to 100.0).forEach { (latitude, longitude) ->
            assertEquals(7.0, nearest.valueAt(singleNode, LatLongPosition(latitude, longitude)))
        }
    }

    @Test
    fun `nearest returns NaN when one of the nearest cells is missing`() {
        val gridWithHole = ArrayRasterGrid(
            latitudes = ImmutableAxis(doubleArrayOf(10.0, 20.0)),
            longitudes = ImmutableAxis(doubleArrayOf(-100.0, 0.0)),
            gridValues = doubleArrayOf(Double.NaN, 10.0, 20.0, 100.0), // south-west is missing
        )
        val value = nearest.valueAt(gridWithHole, LatLongPosition(11.0, -95.0))
        assertTrue(value.isNaN(), "Expected NaN, but got $value")
    }

    // Bilinear spatial interpolation strategy tests
    @Test
    fun `bilinear returns the exact corner value on a node hit without interpolating`() {
        assertEquals(southWest, bilinear.valueAt(grid, LatLongPosition(10.0, -100.0)))
        assertEquals(southEast, bilinear.valueAt(grid, LatLongPosition(10.0, 0.0)))
        assertEquals(northWest, bilinear.valueAt(grid, LatLongPosition(20.0, -100.0)))
        assertEquals(northEast, bilinear.valueAt(grid, LatLongPosition(20.0, 0.0)))
    }

    @Test
    fun `bilinear at the cell center is the average of the four corners`() {
        // (0 + 10 + 20 + 100) / 4 = 32.5
        assertEquals(32.5, bilinear.valueAt(grid, LatLongPosition(15.0, -50.0)), tolerance)
    }

    @Test
    fun `bilinear along an edge degenerates to linear interpolation on that edge`() {
        /*
         * lat = 10 lies exactly on the south edge: only SW and SE should contribute in the calculation.
         * halfway in longitude: (0 + 10) / 2 = 5
         */
        assertEquals(5.0, bilinear.valueAt(grid, LatLongPosition(10.0, -50.0)), tolerance)
    }

    @Test
    fun `bilinear weights the corners by their fractional distance`() {
        /*
         * u = (-75 - -100)/100 = 0.25, v = (12.5 - 10)/10 = 0.25
         * 0*0.75*0.75 + 10*0.25*0.75 + 20*0.75*0.25 + 100*0.25*0.25 = 1.875 + 3.75 + 6.25 = 11.875
         */
        assertEquals(11.875, bilinear.valueAt(grid, LatLongPosition(12.5, -75.0)), tolerance)
    }

    @Test
    fun `bilinear reproduces an affine field exactly and brackets the correct cell`() {
        // f(lat, lon) = lat + lon + 200, so (15, 50) should be 265
        assertEquals(265.0, bilinear.valueAt(affineGrid, LatLongPosition(15.0, 50.0)), tolerance)
    }

    @Test
    fun `bilinear propagates NaN when any of the four corners is missing`() {
        for (i in 0..3) {
            val values = doubleArrayOf(0.0, 10.0, 20.0, 30.0)
            values[i] = Double.NaN
            val gridWithHole = ArrayRasterGrid(
                latitudes = ImmutableAxis(doubleArrayOf(10.0, 20.0)),
                longitudes = ImmutableAxis(doubleArrayOf(-100.0, 0.0)),
                gridValues = values,
            )
            val value = bilinear.valueAt(gridWithHole, LatLongPosition(15.0, -50.0))
            assertTrue(value.isNaN(), "Expected NaN with corner $i missing, but got $value")
        }
    }
}
