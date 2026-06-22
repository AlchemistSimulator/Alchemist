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

class TestMissingValue : StringSpec({

    // 3x3 grid, the values are irrelevant
    val grid = ArrayRasterGrid(
        latitudes = doubleArrayOf(10.0, 20.0, 30.0),
        longitudes = doubleArrayOf(100.0, 200.0, 300.0),
        values = doubleArrayOf(
            110.0, 210.0, 310.0,
            120.0, 220.0, 320.0,
            130.0, 230.0, 330.0,
        ),
    )

    /*
     * A fixed set of probe points, deliberately mixing in-grid and out-of-grid coordinates: a
     * MissingValueStrategy must ignore position entirely, so the result must be constant across all
     * of them. (Whether the strategy is actually reached only on the in-extent path is a
     * SpatialSampler concern)
     */
    val probePoints = listOf(
        15.0 to 150.0, // inside the grid
        20.0 to 200.0, // exactly on a node
        5.0 to 50.0, // out of bounds, south-west
        40.0 to 350.0, // out of bounds, north-east
    )

    // NAN missing value strategy test
    "NAN returns NaN regardless of grid and position" {
        for ((latitude, longitude) in probePoints) {
            withClue("at ($latitude, $longitude)") {
                MissingValue.NAN.valueAt(grid, latitude, longitude).shouldBeNaN()
            }
        }
    }

    // ZERO missing value strategy test
    "ZERO returns 0.0 regardless of grid and position" {
        for ((latitude, longitude) in probePoints) {
            withClue("at ($latitude, $longitude)") {
                MissingValue.ZERO.valueAt(grid, latitude, longitude) shouldBe 0.0
            }
        }
    }
})
