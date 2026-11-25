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
 * Walking speeds, in meters per second, by age and gender.
 *
 * @property default the speed of pedestrians whose age and gender are not known.
 * @property variance the most a random factor adds to a speed.
 */
@Serializable
internal data class SpeedParameters(
    val childMale: Double,
    val adultMale: Double,
    val elderlyMale: Double,
    val childFemale: Double,
    val adultFemale: Double,
    val elderlyFemale: Double,
    val default: Double,
    val variance: Double,
)
