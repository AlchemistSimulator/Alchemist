/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.impact

import kotlin.test.Test
import kotlin.test.assertEquals

class ImpactParametersTest {
    private val parameters = ImpactParameters.default

    @Test
    fun `the parameters file is read in every section`() {
        assertEquals(0.5, parameters.cognitive.sensingOmega)
        assertEquals(0.14, parameters.cognitive.advancedLogisticTau)
        assertEquals(1.0, parameters.speed.default)
        assertEquals(0.97, parameters.compliance.elderlyFemale)
    }

    @Test
    fun `help attitudes are read as pairs, within and outside the group`() {
        assertEquals(0.30 to 0.25, parameters.helpAttitude.adultMale.childMale)
        assertEquals(0.10 to 0.08, parameters.helpAttitude.elderlyFemale.elderlyFemale)
    }
}
