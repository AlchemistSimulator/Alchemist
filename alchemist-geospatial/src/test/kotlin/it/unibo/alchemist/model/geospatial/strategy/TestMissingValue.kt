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
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geospatial.strategy.missing.ConstantDoubleValue
import it.unibo.alchemist.model.geospatial.strategy.missing.ConstantValue
import it.unibo.alchemist.model.geospatial.strategy.missing.MissingValueStrategy
import it.unibo.alchemist.model.geospatial.strategy.missing.PropagateNull

class TestMissingValue : StringSpec({

    lateinit var strategy: MissingValueStrategy<Double>

    "ConstantValue with a non-null value returns that value" {
        strategy = ConstantValue(0.0)
        strategy.value() shouldBe 0.0

        strategy = ConstantValue(-999.0)
        strategy.value() shouldBe -999.0
    }

    "ConstantValue with a null value returns null" {
        strategy = ConstantValue(null)
        strategy.value().shouldBeNull()
    }

    "ConstantValue works for non-Double types too" {
        val stringStrategy: MissingValueStrategy<String> = ConstantValue("fallback")
        stringStrategy.value() shouldBe "fallback"
    }

    "ConstantDoubleValue delegates to ConstantValue with the given replacement" {
        strategy = ConstantDoubleValue(0.0)
        strategy.value() shouldBe 0.0

        strategy = ConstantDoubleValue(-999.0)
        strategy.value() shouldBe -999.0
    }

    "PropagateNull returns null regardless of T" {
        strategy = PropagateNull()
        strategy.value().shouldBeNull()

        val stringStrategy: MissingValueStrategy<String> = PropagateNull()
        stringStrategy.value().shouldBeNull()
    }
})
