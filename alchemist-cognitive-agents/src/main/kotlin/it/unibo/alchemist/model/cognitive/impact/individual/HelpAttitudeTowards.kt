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
 * The probabilities of helping someone, by their age and gender: the first if in the same group, the second if not.
 */
@Serializable
internal data class HelpAttitudeTowards(
    val childMale: Pair<Double, Double>,
    val adultMale: Pair<Double, Double>,
    val elderlyMale: Pair<Double, Double>,
    val childFemale: Pair<Double, Double>,
    val adultFemale: Pair<Double, Double>,
    val elderlyFemale: Pair<Double, Double>,
)
