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
import io.kotest.matchers.shouldBe

class TestTemporalInterpolation : StringSpec({

    val tolerance = 1e-9

    val valueBefore = 10.0
    val valueAfter = 30.0

    // weights spanning in [0, 1]
    val steps = 20
    val weights = (0..steps).map { it.toDouble() / steps }

    // LINEAR temporal interpolation strategy tests
    "LINEAR returns the endpoints at weight 0 and 1" {
        TemporalInterpolation.LINEAR.interpolate(
            valueBefore,
            valueAfter,
            0.0,
        ) shouldBe (valueBefore plusOrMinus tolerance)

        TemporalInterpolation.LINEAR.interpolate(
            valueBefore,
            valueAfter,
            1.0,
        ) shouldBe (valueAfter plusOrMinus tolerance)
    }

    "LINEAR blends proportionally to the weight" {
        for (weight in weights) {
            val expected = valueBefore + (valueAfter - valueBefore) * weight
            withClue("at weight $weight") {
                TemporalInterpolation.LINEAR.interpolate(
                    valueBefore,
                    valueAfter,
                    weight,
                ) shouldBe (expected plusOrMinus tolerance)
            }
        }
    }

    // BEFORE temporal interpolation strategy test
    "BEFORE always returns the earlier value regardless of weight" {
        for (weight in weights) {
            withClue("at weight $weight") {
                TemporalInterpolation.BEFORE.interpolate(valueBefore, valueAfter, weight) shouldBe valueBefore
            }
        }
    }

    // AFTER temporal interpolation strategy test
    "AFTER always returns the later value regardless of weight" {
        for (weight in weights) {
            withClue("at weight $weight") {
                TemporalInterpolation.AFTER.interpolate(valueBefore, valueAfter, weight) shouldBe valueAfter
            }
        }
    }

    // NEAREST temporal interpolation strategy tests
    "NEAREST returns the earlier value when the weight is below 0.5" {
        for (weight in listOf(0.0, 0.25, 0.499)) {
            withClue("at weight $weight") {
                TemporalInterpolation.NEAREST.interpolate(valueBefore, valueAfter, weight) shouldBe valueBefore
            }
        }
    }

    "NEAREST returns the later value when the weight is at or above 0.5" {
        for (weight in listOf(0.5, 0.75, 1.0)) {
            withClue("at weight $weight") {
                TemporalInterpolation.NEAREST.interpolate(valueBefore, valueAfter, weight) shouldBe valueAfter
            }
        }
    }

    "NEAREST resolves the exact 0.5 tie to the later value" {
        TemporalInterpolation.NEAREST.interpolate(valueBefore, valueAfter, 0.5) shouldBe valueAfter
    }
})
