/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.jakta.timedistributions

import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution

data class JaktaTimeDistribution(
    val sense: TimeDistribution,
    val deliberate: TimeDistribution,
    val act: TimeDistribution,
) : TimeDistribution {
    override fun sample(): Time = doNotUse()

    private fun doNotUse(): Nothing = error(
        "${this::class.simpleName} is not meant to be used directly, but to host custom time distributions" +
            " for the sense, deliberate, and act phases of the JaKtA lifecycle.",
    )

    override fun toString(): String = "JaktaTimeDistribution(sense=$sense, deliberate=$deliberate, act=$act)"
}
