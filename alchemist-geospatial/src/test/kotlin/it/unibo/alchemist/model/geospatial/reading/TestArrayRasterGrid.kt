/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe

class TestArrayRasterGrid : StringSpec({

    /**
     * 3 latitudes by 4 longitudes.
     * The cell value at (iLat, iLon) is iLat * 10 + iLon,
     * so it is easily verifiable.
     */
    val lats = doubleArrayOf(10.0, 20.0, 30.0)
    val lons = doubleArrayOf(5.0, 15.0, 25.0, 35.0)
    val values = DoubleArray(12) { idx -> (idx / lons.size) * 10.0 + (idx % lons.size) }
    val grid = ArrayRasterGrid(lats, lons, values)

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

    // Missing values tests
    "valueAt should preserve Double.NaN for missing values" {
        // (iLat=1, iLon=1) is a NaN
        val nanValues = DoubleArray(12) { idx -> if (idx == 5) Double.NaN else idx.toDouble() }
        val nanGrid = ArrayRasterGrid(lats, lons, nanValues)
        nanGrid.valueAt(1, 1).shouldBeNaN()
    }

    "a grid entirely made of NaN should return NaN everywhere" {
        val allNaN = ArrayRasterGrid(lats, lons, DoubleArray(12) { Double.NaN })
        for (iLat in 0..2) {
            for (iLon in 0..3) {
                allNaN.valueAt(iLat, iLon).shouldBeNaN()
            }
        }
    }

    // Axis tests
    "latitudes should be accessible and match the constructor argument" {
        grid.latitudes shouldBe lats
    }

    "longitudes should be accessible and match the constructor argument" {
        grid.longitudes shouldBe lons
    }
})
