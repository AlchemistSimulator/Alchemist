/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TimeDistribution

class TestTriggerReaction<T, P : Position<P>>(
    environment: Environment<T, P>,
    timeDistribution: TimeDistribution<T>,
) : AbstractGlobalReaction<T, P>(environment, timeDistribution) {
    private var executed = false

    override fun executeBeforeUpdateDistribution() {
        when (executed) {
            true -> throw IllegalStateException("Reaction already executed")
            false -> executed = true
        }
    }
}
