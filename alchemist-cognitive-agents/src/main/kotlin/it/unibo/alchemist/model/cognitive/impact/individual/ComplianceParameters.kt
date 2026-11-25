/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.impact.individual

import kotlinx.serialization.Serializable

/**
 * Compliance levels by age and gender.
 */
@Serializable
internal data class ComplianceParameters(
    val childMale: Double,
    val adultMale: Double,
    val elderlyMale: Double,
    val childFemale: Double,
    val adultFemale: Double,
    val elderlyFemale: Double,
)
