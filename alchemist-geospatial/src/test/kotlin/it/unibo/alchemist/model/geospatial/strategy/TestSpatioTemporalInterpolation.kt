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
import it.unibo.alchemist.model.geospatial.strategy.spatiotemporal.TrilinearInterpolation

class TestSpatioTemporalInterpolation : StringSpec({

    val trilinear = TrilinearInterpolation()
    val tolerance = 1e-9

    /*
     * Two 2x2 slices sharing the same spatial grid.
     * gridAfter corners: every value is gridBefore's value + 1000, to keep the time axis
     * easily distinguishable from the space axes.
     */
    val latitudes = doubleArrayOf(10.0, 20.0)
    val longitudes = doubleArrayOf(100.0, 200.0)

    val gridBefore = ArrayRasterGrid(
        latitudes = latitudes,
        longitudes = longitudes,
        values = doubleArrayOf(0.0, 10.0, 20.0, 100.0),
    )
    val gridAfter = ArrayRasterGrid(
        latitudes = latitudes,
        longitudes = longitudes,
        values = doubleArrayOf(1000.0, 1010.0, 1020.0, 1100.0),
    )

    val center = mockGeoPosition(15.0, 150.0) // cell center

    "Trilinear at timeWeight 0.0 equals the bilinear value of gridBefore" {
        // bilinear average of gridBefore corners: (0 + 10 + 20 + 100) / 4 = 32.5
        trilinear.interpolate(center, gridBefore, gridAfter, 0.0) shouldBe (32.5 plusOrMinus tolerance)
    }

    "Trilinear at timeWeight 1.0 equals the bilinear value of gridAfter" {
        // bilinear average of gridAfter corners: (1000 + 1010 + 1020 + 1100) / 4 = 1032.5
        trilinear.interpolate(center, gridBefore, gridAfter, 1.0) shouldBe (1032.5 plusOrMinus tolerance)
    }

    "Trilinear at timeWeight 0.5 is the midpoint between the two bilinear slices" {
        // (32.5 + 1032.5) / 2 = 532.5
        trilinear.interpolate(center, gridBefore, gridAfter, 0.5) shouldBe (532.5 plusOrMinus tolerance)
    }

    "Trilinear propagates NaN when a corner is missing" {
        val gridBeforeWithHole = ArrayRasterGrid(
            latitudes = latitudes,
            longitudes = longitudes,
            values = doubleArrayOf(Double.NaN, 10.0, 20.0, 100.0),
        )
        trilinear.interpolate(center, gridBeforeWithHole, gridAfter, 0.5).shouldBeNaN()
    }

    /*
     * At an exact boundary (timeWeight 0.0 or 1.0), the other slice's weight is
     * zero: a hole in the irrelevant slice must NOT contaminate the result.
     */
    "Trilinear at timeWeight 0.0 ignores a hole in gridAfter" {
        val gridAfterWithHole = ArrayRasterGrid(
            latitudes = latitudes,
            longitudes = longitudes,
            values = doubleArrayOf(Double.NaN, 1010.0, 1020.0, 1100.0),
        )
        trilinear.interpolate(center, gridBefore, gridAfterWithHole, 0.0) shouldBe (32.5 plusOrMinus tolerance)
    }

    "Trilinear at timeWeight 1.0 ignores a hole in gridBefore" {
        val gridBeforeWithHole = ArrayRasterGrid(
            latitudes = latitudes,
            longitudes = longitudes,
            values = doubleArrayOf(Double.NaN, 10.0, 20.0, 100.0),
        )
        trilinear.interpolate(center, gridBeforeWithHole, gridAfter, 1.0) shouldBe (1032.5 plusOrMinus tolerance)
    }
})
