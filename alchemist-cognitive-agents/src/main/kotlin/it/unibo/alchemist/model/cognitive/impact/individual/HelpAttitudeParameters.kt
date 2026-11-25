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
 * The help attitudes of adults and elderly people, by gender.
 */
@Serializable
internal data class HelpAttitudeParameters(
    val adultMale: HelpAttitudeTowards,
    val adultFemale: HelpAttitudeTowards,
    val elderlyMale: HelpAttitudeTowards,
    val elderlyFemale: HelpAttitudeTowards,
)
