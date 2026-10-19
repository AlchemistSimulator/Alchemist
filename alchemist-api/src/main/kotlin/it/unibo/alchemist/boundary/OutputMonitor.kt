/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time

/**
 * An observer of the simulation, for instance for its visualization.
 *
 * Thread safety note: no specific policy is defined for the control flow executing the callbacks.
 * A new thread could have been spawned, or the same flow of the simulation may execute them.
 * This depends on the specific [it.unibo.alchemist.core.Simulation] implementation.
 *
 * @param T concentration type
 * @param P [Position] type
 */
interface OutputMonitor<T, P : Position<out P>> {
    /**
     * Called once the whole simulation has finished, either because it reached its latest point
     * or because the user stopped it, with the [environment], the final [time], and the last [step].
     */
    fun finished(environment: Environment<T, P>, time: Time, step: Long) = Unit

    /**
     * Called as soon as the initialization phase of [environment] is completed.
     */
    fun initialized(environment: Environment<T, P>) = Unit

    /**
     * Called every time a simulation step is done, with the [environment], the last executed [reaction]
     * (if any), the current [time], and the current [step].
     */
    fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) = Unit
}
