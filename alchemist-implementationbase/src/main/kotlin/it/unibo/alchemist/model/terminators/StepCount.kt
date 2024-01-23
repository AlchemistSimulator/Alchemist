/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.terminators

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import java.util.function.Predicate

/**
 * Terminates the simulation after a given number of steps.
 * If the simulation is uninitialized (thus, the environment returns null when asked for the simulation),
 * this predicate always returns false.
 *
 * @param lastStep the last step.
 */
data class StepCount<T, P : Position<P>>(val lastStep: Long) : Predicate<Environment<T, P>> {
    override fun test(environment: Environment<T, P>): Boolean =
        environment.simulation?.step?.let { it >= lastStep } ?: false
}
