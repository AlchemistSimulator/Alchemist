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
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geospatial.reading.ArrayRasterGrid
import it.unibo.alchemist.model.geospatial.reading.RasterGrid

/**
 * A recording strategy that satisfies all three spatial interfaces (they share the same signature),
 * so that this class can play any role. Each instance counts how many times it has been invoked,
 * allowing tests to accurately verify which strategy the SpatialSampler has reached.
 *
 * @param result the fixed value that this mock strategy must return.
 */
private class SpyStrategy(private val result: Double) :
    SpatialInterpolationStrategy, SpatialExtrapolationStrategy, MissingValueStrategy {
    var calls = 0
        private set

    override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double {
        calls++
        return result
    }
}

class TestSpatialSampler : StringSpec({

    val tolerance = 1e-9

    /*
     * Affine 3x3 grid (node value = lat + lon). Bounds: lat 10..30, lon 100..300.
     */
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
     * 2x2 grid with the south-west corner missing, used to exercise NaN handling and the
     * out-of-bounds vs missing asymmetry. Bounds: lat 10..20, lon 100..200.
     */
    val holeGrid = ArrayRasterGrid(
        latitudes = doubleArrayOf(10.0, 20.0),
        longitudes = doubleArrayOf(100.0, 200.0),
        values = doubleArrayOf(Double.NaN, 10.0, 20.0, 100.0),
    )

    val outOfBoundsPoints = listOf(
        5.0 to 150.0,
        35.0 to 150.0,
        15.0 to 50.0,
        15.0 to 350.0,
        5.0 to 50.0,
        180.0 to 180.0,
    )

    val boundaryPoints = listOf(
        10.0 to 150.0,
        30.0 to 150.0,
        15.0 to 100.0,
        15.0 to 300.0,
        10.0 to 100.0,
        30.0 to 300.0,
    )

    // tests with mock strategies
    "in bounds with a finite interpolation returns it and consults neither extrapolation nor missing" {
        val interpolation = SpyStrategy(42.0)
        val outOfBounds = SpyStrategy(99.0)
        val missing = SpyStrategy(-1.0)
        val sampler = SpatialSampler(interpolation, outOfBounds, missing)

        sampler.sample(grid, 15.0, 150.0) shouldBe 42.0
        interpolation.calls shouldBe 1
        outOfBounds.calls shouldBe 0
        missing.calls shouldBe 0
    }

    "in bounds with a NaN interpolation delegates to missing and returns its value" {
        val interpolation = SpyStrategy(Double.NaN)
        val outOfBounds = SpyStrategy(99.0)
        val missing = SpyStrategy(7.0)
        val sampler = SpatialSampler(interpolation, outOfBounds, missing)

        sampler.sample(grid, 15.0, 150.0) shouldBe 7.0
        interpolation.calls shouldBe 1
        missing.calls shouldBe 1
        outOfBounds.calls shouldBe 0
    }

    "out of bounds delegates to extrapolation and consults neither interpolation nor missing" {
        for ((latitude, longitude) in outOfBoundsPoints) {
            val interpolation = SpyStrategy(42.0)
            val outOfBounds = SpyStrategy(99.0)
            val missing = SpyStrategy(-1.0)
            val sampler = SpatialSampler(interpolation, outOfBounds, missing)

            withClue("at ($latitude, $longitude)") {
                sampler.sample(grid, latitude, longitude) shouldBe 99.0
                outOfBounds.calls shouldBe 1
                interpolation.calls shouldBe 0
                missing.calls shouldBe 0
            }
        }
    }

    "points exactly on the grid boundary are treated as IN bounds" {
        for ((latitude, longitude) in boundaryPoints) {
            val interpolation = SpyStrategy(42.0)
            val outOfBounds = SpyStrategy(99.0)
            val missing = SpyStrategy(-1.0)
            val sampler = SpatialSampler(interpolation, outOfBounds, missing)

            withClue("at ($latitude, $longitude)") {
                sampler.sample(grid, latitude, longitude) shouldBe 42.0
                interpolation.calls shouldBe 1
                outOfBounds.calls shouldBe 0
            }
        }
    }

    // tests with real strategies
    "composed NEAREST + ZERO + NAN samples in bounds and zeroes out of bounds" {
        val sampler = SpatialSampler(
            SpatialInterpolation.NEAREST,
            SpatialExtrapolation.ZERO,
            MissingValue.NAN,
        )
        sampler.sample(grid, 20.0, 200.0) shouldBe 220.0
        sampler.sample(grid, 5.0, 50.0) shouldBe 0.0
    }

    "composed BILINEAR propagates an in-grid NaN through MissingValue.NAN" {
        val sampler = SpatialSampler(
            SpatialInterpolation.BILINEAR,
            SpatialExtrapolation.ZERO,
            MissingValue.NAN,
        )
        /*
         * (15, 150) is the cell center and SW corner is NaN, so bilinear should return NaN which
         * should be propagated by MissingValue
         */
        sampler.sample(holeGrid, 15.0, 150.0).shouldBeNaN()
    }

    "composed BILINEAR substitutes an in-grid NaN through MissingValue.ZERO" {
        val sampler = SpatialSampler(
            SpatialInterpolation.BILINEAR,
            SpatialExtrapolation.ZERO,
            MissingValue.ZERO,
        )
        sampler.sample(holeGrid, 15.0, 150.0) shouldBe (0.0 plusOrMinus tolerance)
    }

    "an in-extent NaN is substituted by missing, but an out-of-extent NaN is not" {
        // in bounds: nearest hits the missing SW node, MissingValue.ZERO substitutes
        val inExtent = SpatialSampler(
            SpatialInterpolation.NEAREST,
            SpatialExtrapolation.ZERO,
            MissingValue.ZERO,
        )
        inExtent.sample(holeGrid, 10.0, 100.0) shouldBe 0.0

        // out of bounds: NEAREST_EDGE clamps to the same missing SW node. NaN returned WITHOUT substitution
        val outOfExtent = SpatialSampler(
            SpatialInterpolation.NEAREST,
            SpatialExtrapolation.NEAREST_EDGE,
            MissingValue.ZERO,
        )
        outOfExtent.sample(holeGrid, 5.0, 50.0).shouldBeNaN()
    }
})
