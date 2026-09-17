/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.stringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.mockGeoPosition

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
fun rasterGridContract(gridOf: (DoubleArray, DoubleArray, DoubleArray) -> RasterGrid) = stringSpec {
    /**
     * 3 latitudes by 4 longitudes.
     * The cell value at (iLat, iLon) is iLat * 10 + iLon,
     * so it is easily verifiable.
     */
    val lats = doubleArrayOf(10.0, 20.0, 30.0)
    val lons = doubleArrayOf(5.0, 15.0, 25.0, 35.0)
    val values = DoubleArray(12) { idx -> idx / lons.size * 10.0 + (idx % lons.size) }
    val grid = gridOf(lats, lons, values)

    // Value access tests
    "valueAt should return the correct value for an interior cell" {
        // (iLat=1, iLong=2), then index = 1*4 + 2 = 6, then value = 1*10 + 2 = 12
        grid.valueAt(1, 2) shouldBe 12.0
    }

    "valueAt should return correct values at all four corners" {
        grid.valueAt(0, 0) shouldBe 0.0 // bottom left corner
        grid.valueAt(0, 3) shouldBe 3.0 // bottom right
        grid.valueAt(2, 0) shouldBe 20.0 // top left
        grid.valueAt(2, 3) shouldBe 23.0 // top right
    }

    // Spatial coverage check
    "isInBounds should return true for every position within the spatial extent" {
        val steps = 40
        cartesian(
            linspace(lats.first(), lats.last(), steps),
            linspace(lons.first(), lons.last(), steps),
        ).forAll { (lat, lon) ->
            grid.isInBounds(mockGeoPosition(lat, lon)).shouldBeTrue()
        }
    }

    "isInBounds should return false when a coordinate falls outside the spatial extent" {
        val offset = 0.5
        val midLat = lats[lats.size / 2]
        val midLon = lons[lons.size / 2]
        listOf(
            lats.first() - offset to midLon,
            lats.last() + offset to midLon,
            midLat to lons.first() - offset,
            midLat to lons.last() + offset,
        ).forAll { (lat, lon) ->
            grid.isInBounds(mockGeoPosition(lat, lon)).shouldBeFalse()
        }
    }

    // Missing values tests
    "valueAt should return Double.NaN for missing values" {
        // (iLat=1, iLon=1) is a missing value
        val nanValues = DoubleArray(12) { idx -> if (idx == 5) Double.NaN else idx.toDouble() }
        val nullGrid = ArrayRasterGrid(lats, lons, nanValues)
        nullGrid.valueAt(1, 1).shouldBeNaN()
    }

    "a grid entirely made of Double.NaN should return NaN everywhere" {
        val allNan = ArrayRasterGrid(lats, lons, DoubleArray(12) { Double.NaN })
        cartesian(lats.indices, lons.indices).forAll { (iLat, iLon) ->
            allNan.valueAt(iLat, iLon).shouldBeNaN()
        }
    }

    // Axis tests
    "latitudes should be accessible and match the constructor argument" {
        grid.latitudes shouldBe lats
    }

    "longitudes should be accessible and match the constructor argument" {
        grid.longitudes shouldBe lons
    }

    // Dimension mismatch
    "a mismatch between (lats x lons) and values should raise an exception" {
        shouldThrow<IllegalArgumentException> {
            gridOf(
                lats,
                lons,
                DoubleArray(lats.size * lons.size - 1) { 0.0 },
            )
        }
    }

    "axes that are not strictly increasing should raise an exception" {
        shouldThrow<IllegalArgumentException> {
            gridOf(
                lats.reversedArray(),
                lons,
                values,
            )
        }
        shouldThrow<IllegalArgumentException> {
            gridOf(
                lats,
                lons.reversedArray(),
                values,
            )
        }
    }
}

// tests the array implementation (suitable for dense grids)
class TestArrayRasterGrid : StringSpec({
    include(rasterGridContract(::ArrayRasterGrid))
})

// tests the map implementation (suitable for sparse grids)
class TestMapRasterGrid : StringSpec({
    include(rasterGridContract(::MapRasterGrid))
})
