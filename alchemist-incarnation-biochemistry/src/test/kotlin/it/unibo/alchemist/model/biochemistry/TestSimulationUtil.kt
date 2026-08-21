/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time

fun <T, P : Position<out P>> Environment<T, P>.startSimulation(
    initialized: (e: Environment<T, P>) -> Unit,
    stepDone: (e: Environment<T, P>, r: Reaction<T>?, t: Time, s: Long) -> Unit,
    finished: (e: Environment<T, P>, t: Time, s: Long) -> Unit,
) {
    with(Engine(this)) {
        addOutputMonitor(
            object : OutputMonitor<T, P> {
                override fun initialized(environment: Environment<T, P>) = initialized.invoke(environment)

                override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, t: Time, s: Long) =
                    stepDone.invoke(environment, reaction, t, s)

                override fun finished(environment: Environment<T, P>, t: Time, s: Long) =
                    finished.invoke(environment, t, s)
            },
        )
        play()
        run()
        error.ifPresent { throw it }
    }
}

fun <T, P : Position<out P>> Environment<T, P>.startSimulationWithoutParameters(
    initialized: () -> Unit = { },
    stepDone: () -> Unit = { },
    finished: () -> Unit = { },
) = startSimulation(
    { initialized.invoke() },
    { _, _, _, _ -> stepDone.invoke() },
    { _, _, _ -> finished.invoke() },
)
