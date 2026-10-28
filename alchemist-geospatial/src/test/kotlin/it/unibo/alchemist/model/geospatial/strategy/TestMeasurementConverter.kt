/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import it.unibo.alchemist.model.geospatial.strategy.converter.DoubleIdentityWithFallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestMeasurementConverter {

    private val doubleIdentity = DoubleIdentityWithFallback()

    @Test
    fun `Double identity gets preserved`() {
        (-99..99).forEach { i ->
            val value = i.toDouble()
            assertEquals(value, doubleIdentity.convert(value))
        }
    }

    @Test
    fun `Double identity yields NaN by default on NaN`() {
        val converted = doubleIdentity.convert(Double.NaN)
        assertTrue(converted.isNaN(), "expected NaN, but got $converted")
    }

    @Test
    fun `Double identity replaces NaN with a default value`() {
        val defaultValue = -1.0
        val id = DoubleIdentityWithFallback(defaultValue)
        assertEquals(42.0, id.convert(42.0))
        assertEquals(defaultValue, id.convert(Double.NaN))
    }
}
