/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.terminators

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.Time

/**
 * Terminates the simulation when a given time is reached.
 *
 * @param T the concentration type
 * @param P the position type
 * @property endTime the end time at which the simulation should terminate
 */
data class AfterTime<T, P : Position<P>>(val endTime: Time) : TerminationPredicate<T, P> {
    override fun invoke(environment: Environment<T, P>) = environment.simulation.time >= endTime
}
