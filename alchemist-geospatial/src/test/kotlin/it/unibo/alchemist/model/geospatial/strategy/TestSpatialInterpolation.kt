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
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geospatial.reading.ArrayRasterGrid
import it.unibo.alchemist.model.geospatial.strategy.spatial.BilinearInterpolator
import it.unibo.alchemist.model.geospatial.strategy.spatial.NearestDoubleInterpolation
import it.unibo.alchemist.model.geospatial.strategy.spatial.SpatialInterpolationStrategy

class TestSpatialInterpolation : StringSpec({

    lateinit var interpolator: SpatialInterpolationStrategy<Double>

    val tolerance = 1e-9

    /*
     * 2x2 grid with NON-AFFINE corners (i.e. NE != SE + NW - SW), so that bilinear
     * exercises the u*v cross terms.
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
        values = arrayOf(southWest, southEast, northWest, northEast),
    )

    /*
     * 3x3 grid used to verify correct cell selection in a multi-cell grid: on an affine field
     * bilinear reproduces the value exactly, so the expected result is just lat + lon.
     */
    val affineGrid = ArrayRasterGrid(
        latitudes = doubleArrayOf(10.0, 20.0, 30.0),
        longitudes = doubleArrayOf(100.0, 200.0, 300.0),
        values = arrayOf(
            110.0, 210.0, 310.0, // lat = 10
            120.0, 220.0, 320.0, // lat = 20
            130.0, 230.0, 330.0, // lat = 30
        ),
    )

    // NEAREST spatial interpolation strategy tests
    "NEAREST returns the cell value on an exact node hit" {
        interpolator = NearestDoubleInterpolation()
        interpolator.valueAt(grid, GeoPositionMock(10.0, 100.0)) shouldBe southWest
        interpolator.valueAt(grid, GeoPositionMock(20.0, 200.0)) shouldBe northEast
    }

    "NEAREST picks the closest cell" {
        interpolator = NearestDoubleInterpolation()
        // closest to south-west
        interpolator.valueAt(grid, GeoPositionMock(12.0, 110.0)) shouldBe southWest
        // closest to north-east
        interpolator.valueAt(grid, GeoPositionMock(18.0, 190.0)) shouldBe northEast
    }

    "NEAREST resolves an exact tie to the lower index on each axis" {
        interpolator = NearestDoubleInterpolation()
        // lat 15 and lon 150 are both exactly between nodes, so the lower index should be picked
        interpolator.valueAt(grid, GeoPositionMock(15.0, 150.0)) shouldBe southWest
    }

    "NEAREST selects the correct cell in a multi-cell grid" {
        interpolator = NearestDoubleInterpolation()
        // nearest node to (22, 290) is (20, 300)
        interpolator.valueAt(affineGrid, GeoPositionMock(22.0, 290.0)) shouldBe 320.0
    }

    "NEAREST returns null when one of the nearest cells is missing" {
        interpolator = NearestDoubleInterpolation()
        val gridWithHole = ArrayRasterGrid(
            latitudes = doubleArrayOf(10.0, 20.0),
            longitudes = doubleArrayOf(100.0, 200.0),
            values = arrayOf(null, 10.0, 20.0, 100.0), // south-west is missing
        )
        interpolator.valueAt(gridWithHole, GeoPositionMock(11.0, 105.0)).shouldBeNull()
    }

    // BILINEAR spatial interpolation strategy tests
    "BILINEAR returns the exact corner value on a node hit (no interpolation)" {
        interpolator = BilinearInterpolator()
        interpolator.valueAt(grid, GeoPositionMock(10.0, 100.0)) shouldBe southWest
        interpolator.valueAt(grid, GeoPositionMock(10.0, 200.0)) shouldBe southEast
        interpolator.valueAt(grid, GeoPositionMock(20.0, 100.0)) shouldBe northWest
        interpolator.valueAt(grid, GeoPositionMock(20.0, 200.0)) shouldBe northEast
    }

    "BILINEAR at the cell center is the average of the four corners" {
        interpolator = BilinearInterpolator()
        // (0 + 10 + 20 + 100) / 4 = 32.5
        interpolator.valueAt(grid, GeoPositionMock(15.0, 150.0)) shouldBe (32.5 plusOrMinus tolerance)
    }

    "BILINEAR along an edge degenerates to linear interpolation on that edge" {
        interpolator = BilinearInterpolator()
        /*
         * lat = 10 lies exactly on the south edge: only SW and SE should contribute in the calculation.
         * halfway in longitude: (0 + 10) / 2 = 5
         */
        interpolator.valueAt(grid, GeoPositionMock(10.0, 150.0)) shouldBe (5.0 plusOrMinus tolerance)
    }

    "BILINEAR weights the corners by their fractional distance" {
        interpolator = BilinearInterpolator()
        /*
         * u = (125 - 100)/100 = 0.25, v = (12.5 - 10)/10 = 0.25
         * 0*0.75*0.75 + 10*0.25*0.75 + 20*0.75*0.25 + 100*0.25*0.25 = 1.875 + 3.75 + 6.25 = 11.875
         */
        interpolator.valueAt(grid, GeoPositionMock(12.5, 125.0)) shouldBe (11.875 plusOrMinus tolerance)
    }

    "BILINEAR reproduces an affine field exactly and brackets the correct cell" {
        interpolator = BilinearInterpolator()
        // f(lat, lon) = lat + lon, so (15, 250) should be 265
        interpolator.valueAt(affineGrid, GeoPositionMock(15.0, 250.0)) shouldBe (265.0 plusOrMinus tolerance)
    }

    "BILINEAR propagates null when any of the four corners is missing" {
        interpolator = BilinearInterpolator()
        for (i in 0..3) {
            val values = arrayOf<Double?>(0.0, 10.0, 20.0, 30.0)
            values[i] = null
            val gridWithHole = ArrayRasterGrid(
                latitudes = doubleArrayOf(10.0, 20.0),
                longitudes = doubleArrayOf(100.0, 200.0),
                values = values,
            )
            interpolator.valueAt(gridWithHole, GeoPositionMock(15.0, 150.0)).shouldBeNull()
        }
    }
})

/**
 * [GeoPosition] mock used in this test class.
 */
private class GeoPositionMock(
    override val x: Double,
    override val y: Double,
    override val coordinates: DoubleArray = doubleArrayOf(x, y),
    override val dimensions: Int = 2,
) : GeoPosition {

    override fun getLatitude(): Double = x

    override fun getLongitude(): Double = y

    @Deprecated("Deprecated in Java")
    override fun getCoordinate(dimension: Int): Double = coordinates[dimension]

    // methods not used in the tests
    override fun plus(other: GeoPosition?): GeoPosition = TODO("Not needed for tests")
    override fun minus(other: GeoPosition?): GeoPosition = TODO("Not needed for tests")
    override fun boundingBox(range: Double): List<GeoPosition> = TODO("Not needed for tests")
    override fun distanceTo(other: GeoPosition?): Double = TODO("Not needed for tests")
    override fun plus(other: DoubleArray): GeoPosition = TODO("Not needed for tests")
    override fun minus(other: DoubleArray): GeoPosition = TODO("Not needed for tests")
}
