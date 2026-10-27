/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

internal class SteppableEngine(
    environment: Continuous2DEnvironment<Double>,
) : Engine<Double, Euclidean2DPosition>(environment) {
    fun initializeForTest() = initialize()

    fun runPendingCommands() {
        while (commands.isNotEmpty()) {
            processCommand(commands.poll())
        }
    }

    fun step() = doStep()
}
