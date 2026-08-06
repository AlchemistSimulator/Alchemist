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
import it.unibo.alchemist.model.geospatial.strategy.temporal.ClosestInterpolation
import it.unibo.alchemist.model.geospatial.strategy.temporal.LastInterpolation
import it.unibo.alchemist.model.geospatial.strategy.temporal.LinearInterpolation
import it.unibo.alchemist.model.geospatial.strategy.temporal.NextInterpolation

class TestTemporalInterpolation : StringSpec({

    val closest = ClosestInterpolation()
    val last = LastInterpolation()
    val next = NextInterpolation()
    val linear = LinearInterpolation()

    val tolerance = 1e-9

    val valueBefore = 10.0
    val valueAfter = 30.0

    // weights spanning in [0, 1]
    val steps = 20
    val weights = (0..steps).map { it.toDouble() / steps }

    // Linear temporal interpolation strategy tests
    "Linear returns the endpoints at weight 0 and 1" {
        linear.interpolate(
            valueBefore,
            valueAfter,
            0.0,
        ) shouldBe (valueBefore plusOrMinus tolerance)

        linear.interpolate(
            valueBefore,
            valueAfter,
            1.0,
        ) shouldBe (valueAfter plusOrMinus tolerance)
    }

    "Linear blends proportionally to the weight" {
        for (weight in weights) {
            val expected = valueBefore + (valueAfter - valueBefore) * weight
            withClue("at weight $weight") {
                linear.interpolate(
                    valueBefore,
                    valueAfter,
                    weight,
                ) shouldBe (expected plusOrMinus tolerance)
            }
        }
    }

    // Last temporal interpolation strategy test
    "Last always returns the earlier value regardless of weight" {
        for (weight in weights) {
            withClue("at weight $weight") {
                last.interpolate(valueBefore, valueAfter, weight) shouldBe valueBefore
            }
        }
    }

    // Next temporal interpolation strategy test
    "Next always returns the later value regardless of weight" {
        for (weight in weights) {
            withClue("at weight $weight") {
                next.interpolate(valueBefore, valueAfter, weight) shouldBe valueAfter
            }
        }
    }

    // Closest temporal interpolation strategy tests
    "Closest returns the earlier value when the weight is below 0.5" {
        for (weight in listOf(0.0, 0.25, 0.499)) {
            withClue("at weight $weight") {
                closest.interpolate(valueBefore, valueAfter, weight) shouldBe valueBefore
            }
        }
    }

    "Closest returns the later value when the weight is at or above 0.5" {
        for (weight in listOf(0.5, 0.75, 1.0)) {
            withClue("at weight $weight") {
                closest.interpolate(valueBefore, valueAfter, weight) shouldBe valueAfter
            }
        }
    }

    "Closest resolves the exact 0.5 tie to the later value" {
        closest.interpolate(valueBefore, valueAfter, 0.5) shouldBe valueAfter
    }
})
