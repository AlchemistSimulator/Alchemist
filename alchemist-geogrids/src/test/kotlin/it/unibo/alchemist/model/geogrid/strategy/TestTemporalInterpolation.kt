/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy

import it.unibo.alchemist.model.geogrid.strategy.temporal.ClosestInterpolation
import it.unibo.alchemist.model.geogrid.strategy.temporal.LastInterpolation
import it.unibo.alchemist.model.geogrid.strategy.temporal.LinearInterpolation
import it.unibo.alchemist.model.geogrid.strategy.temporal.NextInterpolation
import kotlin.test.Test
import kotlin.test.assertEquals

class TestTemporalInterpolation {

    private val closest = ClosestInterpolation()
    private val last = LastInterpolation()
    private val next = NextInterpolation()
    private val linear = LinearInterpolation()

    private val tolerance = 1e-9

    private val valueBefore = 10.0
    private val valueAfter = 30.0

    // weights spanning in [0, 1]
    private val steps = 20
    private val weights = (0..steps).map { it.toDouble() / steps }

    // Linear temporal interpolation strategy tests
    @Test
    fun `linear returns the endpoints at weight 0 and 1`() {
        assertEquals(
            valueBefore,
            linear.interpolate(
                valueBefore,
                valueAfter,
                0.0,
            ),
            tolerance,
        )
        assertEquals(
            valueAfter,
            linear.interpolate(
                valueBefore,
                valueAfter,
                1.0,
            ),
            tolerance,
        )
    }

    @Test
    fun `linear blends proportionally to the weight`() {
        for (weight in weights) {
            val expected = valueBefore + (valueAfter - valueBefore) * weight
            assertEquals(
                expected,
                linear.interpolate(
                    valueBefore,
                    valueAfter,
                    weight,
                ),
                tolerance,
                "at weight $weight",
            )
        }
    }

    // Last temporal interpolation strategy test
    @Test
    fun `last always returns the earlier value regardless of weight`() {
        for (weight in weights) {
            assertEquals(valueBefore, last.interpolate(valueBefore, valueAfter, weight), "at weight $weight")
        }
    }

    // Next temporal interpolation strategy test
    @Test
    fun `next always returns the later value regardless of weight`() {
        for (weight in weights) {
            assertEquals(valueAfter, next.interpolate(valueBefore, valueAfter, weight), "at weight $weight")
        }
    }

    // Closest temporal interpolation strategy tests
    @Test
    fun `closest returns the earlier value when the weight is below one half`() {
        for (weight in listOf(0.0, 0.25, 0.499)) {
            assertEquals(valueBefore, closest.interpolate(valueBefore, valueAfter, weight), "at weight $weight")
        }
    }

    @Test
    fun `closest returns the later value when the weight is at or above one half`() {
        for (weight in listOf(0.5, 0.75, 1.0)) {
            assertEquals(valueAfter, closest.interpolate(valueBefore, valueAfter, weight), "at weight $weight")
        }
    }

    @Test
    fun `closest resolves the exact one half tie to the later value`() {
        assertEquals(valueAfter, closest.interpolate(valueBefore, valueAfter, 0.5))
    }
}
