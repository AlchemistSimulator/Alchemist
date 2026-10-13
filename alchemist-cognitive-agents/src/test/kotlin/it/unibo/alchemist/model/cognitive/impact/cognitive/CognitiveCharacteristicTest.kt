/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.impact.cognitive

import kotlin.test.Test
import kotlin.test.assertEquals

class CognitiveCharacteristicTest {
    @Test
    fun `a body characteristic changes by the same amount per unit of time whatever the update time step`() {
        val coarse = ConstantBodyCharacteristic().apply { update(1.0) }
        val fine = ConstantBodyCharacteristic().apply { repeat(STEPS) { update(1.0 / STEPS) } }
        assertEquals(coarse.level(), fine.level(), TOLERANCE)
    }

    @Test
    fun `a mental characteristic converges at a pace independent of the update time step`() {
        val coarse = ConstantMentalCharacteristic().apply { repeat(STEPS) { update(1.0 / STEPS) } }
        val fine = ConstantMentalCharacteristic().apply { repeat(STEPS * STEPS) { update(1.0 / (STEPS * STEPS)) } }
        assertEquals(coarse.level(), fine.level(), coarse.level() * RELATIVE_TOLERANCE)
    }

    private class ConstantBodyCharacteristic : BodyCognitiveCharacteristic() {
        override fun combinationFunction() = 1.0
    }

    private class ConstantMentalCharacteristic : MentalCognitiveCharacteristic() {
        override fun combinationFunction() = 1.0
    }

    private companion object {
        private const val RELATIVE_TOLERANCE = 0.05
        private const val STEPS = 10
        private const val TOLERANCE = 1e-12
    }
}
