/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geospatial.reading.ArrayRasterGrid

class TestSpatialExtrapolation : StringSpec({

    val grid = ArrayRasterGrid(
        latitudes = doubleArrayOf(10.0, 20.0, 30.0),
        longitudes = doubleArrayOf(100.0, 200.0, 300.0),
        values = doubleArrayOf(
            110.0, 210.0, 310.0, // lat 10
            120.0, 220.0, 320.0, // lat 20
            130.0, 230.0, 330.0, // lat 30
        ),
    )

    /*
     * Fixed out-of-bounds points covering the 8 exterior regions (4 corners and 4 edges). The third
     * value is the expected NEAREST_EDGE result. The in-bounds axis coordinate always sits
     * exactly on a node, so there is no nearest/tie ambiguity to resolve.
     */
    val outOfBoundsPoints = listOf(
        Triple(5.0, 50.0, 110.0), // SW corner
        Triple(5.0, 200.0, 210.0), // S edge
        Triple(5.0, 350.0, 310.0), // SE corner
        Triple(20.0, 350.0, 320.0), // E edge
        Triple(40.0, 350.0, 330.0), // NE corner
        Triple(40.0, 200.0, 230.0), // N edge
        Triple(40.0, 50.0, 130.0), // NW corner
        Triple(20.0, 50.0, 120.0), // W edge
    )

    // ZERO spatial extrapolation strategy test
    "ZERO returns 0.0 for every out-of-bounds point" {
        for ((latitude, longitude, _) in outOfBoundsPoints) {
            withClue("at ($latitude, $longitude)") {
                SpatialExtrapolation.ZERO.valueAt(grid, latitude, longitude) shouldBe 0.0
            }
        }
    }

    // NAN spatial extrapolation strategy test
    "NAN returns NaN for every out-of-bounds point" {
        for ((latitude, longitude, _) in outOfBoundsPoints) {
            withClue("at ($latitude, $longitude)") {
                SpatialExtrapolation.NAN.valueAt(grid, latitude, longitude).shouldBeNaN()
            }
        }
    }

    // NEAREST_EDGE spatial extrapolation strategy tests
    "NEAREST_EDGE clamps to the nearest edge node for every out-of-bounds region" {
        for ((latitude, longitude, expectedEdgeValue) in outOfBoundsPoints) {
            withClue("at ($latitude, $longitude)") {
                SpatialExtrapolation.NEAREST_EDGE.valueAt(grid, latitude, longitude) shouldBe expectedEdgeValue
            }
        }
    }

    "NEAREST_EDGE returns a raw missing edge cell (NaN) without substitution" {
        val gridWithMissingCorner = ArrayRasterGrid(
            latitudes = doubleArrayOf(10.0, 20.0, 30.0),
            longitudes = doubleArrayOf(100.0, 200.0, 300.0),
            values = doubleArrayOf(
                Double.NaN, 210.0, 310.0, // SW corner missing
                120.0, 220.0, 320.0,
                130.0, 230.0, 330.0,
            ),
        )
        // both points lie beyond the south-west corner, so both should clamp to the (missing) SW node
        for ((latitude, longitude) in listOf(5.0 to 50.0, 8.0 to 90.0)) {
            withClue("at ($latitude, $longitude)") {
                SpatialExtrapolation.NEAREST_EDGE.valueAt(gridWithMissingCorner, latitude, longitude).shouldBeNaN()
            }
        }
    }
})
