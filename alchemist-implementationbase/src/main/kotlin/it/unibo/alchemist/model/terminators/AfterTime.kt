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
import it.unibo.alchemist.model.Time
import java.util.function.Predicate

/**
 * @param endTime the end time.
 */
class AfterTime(val endTime: Time) : Predicate<Environment<*, *>> {

    /**
     * Tries to access the simulation time from the [environment].
     * If the simulation is unaccessible, throws an exception.
     * Otherwise, reads the current time, and flips to true once it got past the provided [endTime].
     */
    override fun test(environment: Environment<*, *>): Boolean =
        checkNotNull(environment.simulation?.time?.let { it >= endTime }) {
            "No simulation available for environment $environment, unable to read time."
        }
}
