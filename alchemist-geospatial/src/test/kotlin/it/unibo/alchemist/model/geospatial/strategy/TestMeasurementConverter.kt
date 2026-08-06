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
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geospatial.strategy.converter.DoubleIdentityWithFallback

class TestMeasurementConverter : StringSpec({

    val doubleIdentity = DoubleIdentityWithFallback()

    "Double identity gets preserved" {
        (-99..99).forEach { i ->
            val value = i.toDouble()
            doubleIdentity.convert(value) shouldBe value
        }
    }

    "Double identity yields NaN by default on NaN" {
        doubleIdentity.convert(Double.NaN).shouldBeNaN()
    }

    "Double identity replaces NaN with a default value" {
        val defaultValue = -1.0
        val id = DoubleIdentityWithFallback(defaultValue)

        id.convert(42.0) shouldBe 42.0
        id.convert(Double.NaN) shouldBe defaultValue
    }
})
