/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.mockGeoPosition
import it.unibo.alchemist.model.geospatial.reading.ArrayRasterGrid
import it.unibo.alchemist.model.geospatial.strategy.spatial.BilinearInterpolation
import it.unibo.alchemist.model.geospatial.strategy.spatial.NearestInterpolation

class TestSpatialInterpolation : StringSpec({

    val nearest = NearestInterpolation()
    val bilinear = BilinearInterpolation()

    val tolerance = 1e-9

    /*
     * 2x2 grid.
     * It can be represented as follows:
     *           W(lon 100) E(lon 200)
     * N(lat 20)    20(NW)   100(NE)
     * S(lat 10)    0(SW)    10(SE)
     */
    val southWest = 0.0
    val southEast = 10.0
    val northWest = 20.0
    val northEast = 100.0
    val grid = ArrayRasterGrid(
        latitudes = doubleArrayOf(10.0, 20.0),
        longitudes = doubleArrayOf(100.0, 200.0),
        // row-major representation
        values = doubleArrayOf(southWest, southEast, northWest, northEast),
    )

    /*
     * 3x3 grid used to verify correct cell selection in a multi-cell grid: on an affine field
     * bilinear reproduces the value exactly, so the expected result is just lat + lon.
     */
    val affineGrid = ArrayRasterGrid(
        latitudes = doubleArrayOf(10.0, 20.0, 30.0),
        longitudes = doubleArrayOf(100.0, 200.0, 300.0),
        values = doubleArrayOf(
            110.0, 210.0, 310.0, // lat = 10
            120.0, 220.0, 320.0, // lat = 20
            130.0, 230.0, 330.0, // lat = 30
        ),
    )

    // Nearest spatial interpolation strategy tests
    "Nearest returns the cell value on an exact node hit" {
        nearest.valueAt(grid, mockGeoPosition(10.0, 100.0)) shouldBe southWest
        nearest.valueAt(grid, mockGeoPosition(20.0, 200.0)) shouldBe northEast
    }

    "Nearest picks the closest cell" {
        // closest to south-west
        nearest.valueAt(grid, mockGeoPosition(12.0, 110.0)) shouldBe southWest
        // closest to north-east
        nearest.valueAt(grid, mockGeoPosition(18.0, 190.0)) shouldBe northEast
    }

    "Nearest resolves an exact tie to the lower index on each axis" {
        // lat 15 and lon 150 are both exactly between nodes, so the lower index should be picked
        nearest.valueAt(grid, mockGeoPosition(15.0, 150.0)) shouldBe southWest
    }

    "Nearest selects the correct cell in a multi-cell grid" {
        // nearest node to (22, 290) is (20, 300)
        nearest.valueAt(affineGrid, mockGeoPosition(22.0, 290.0)) shouldBe 320.0
    }

    "Nearest returns NaN when one of the nearest cells is missing" {
        val gridWithHole = ArrayRasterGrid(
            latitudes = doubleArrayOf(10.0, 20.0),
            longitudes = doubleArrayOf(100.0, 200.0),
            values = doubleArrayOf(Double.NaN, 10.0, 20.0, 100.0), // south-west is missing
        )
        nearest.valueAt(gridWithHole, mockGeoPosition(11.0, 105.0)).shouldBeNaN()
    }

    // Bilinear spatial interpolation strategy tests
    "Bilinear returns the exact corner value on a node hit (no interpolation)" {
        bilinear.valueAt(grid, mockGeoPosition(10.0, 100.0)) shouldBe southWest
        bilinear.valueAt(grid, mockGeoPosition(10.0, 200.0)) shouldBe southEast
        bilinear.valueAt(grid, mockGeoPosition(20.0, 100.0)) shouldBe northWest
        bilinear.valueAt(grid, mockGeoPosition(20.0, 200.0)) shouldBe northEast
    }

    "Bilinear at the cell center is the average of the four corners" {
        // (0 + 10 + 20 + 100) / 4 = 32.5
        bilinear.valueAt(grid, mockGeoPosition(15.0, 150.0)) shouldBe (32.5 plusOrMinus tolerance)
    }

    "Bilinear along an edge degenerates to linear interpolation on that edge" {
        /*
         * lat = 10 lies exactly on the south edge: only SW and SE should contribute in the calculation.
         * halfway in longitude: (0 + 10) / 2 = 5
         */
        bilinear.valueAt(grid, mockGeoPosition(10.0, 150.0)) shouldBe (5.0 plusOrMinus tolerance)
    }

    "Bilinear weights the corners by their fractional distance" {
        /*
         * u = (125 - 100)/100 = 0.25, v = (12.5 - 10)/10 = 0.25
         * 0*0.75*0.75 + 10*0.25*0.75 + 20*0.75*0.25 + 100*0.25*0.25 = 1.875 + 3.75 + 6.25 = 11.875
         */
        bilinear.valueAt(grid, mockGeoPosition(12.5, 125.0)) shouldBe (11.875 plusOrMinus tolerance)
    }

    "Bilinear reproduces an affine field exactly and brackets the correct cell" {
        // f(lat, lon) = lat + lon, so (15, 250) should be 265
        bilinear.valueAt(affineGrid, mockGeoPosition(15.0, 250.0)) shouldBe (265.0 plusOrMinus tolerance)
    }

    "Bilinear propagates NaN when any of the four corners is missing" {
        for (i in 0..3) {
            val values = doubleArrayOf(0.0, 10.0, 20.0, 30.0)
            values[i] = Double.NaN
            val gridWithHole = ArrayRasterGrid(
                latitudes = doubleArrayOf(10.0, 20.0),
                longitudes = doubleArrayOf(100.0, 200.0),
                values = values,
            )
            bilinear.valueAt(gridWithHole, mockGeoPosition(15.0, 150.0)).shouldBeNaN()
        }
    }
})
