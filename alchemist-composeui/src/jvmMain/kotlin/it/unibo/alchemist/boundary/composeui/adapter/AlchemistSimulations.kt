/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.Position

/**
 * Turns Alchemist simulations into what the UI shows.
 */
internal object AlchemistSimulations {
    /** The status the UI shows for this simulation. */
    fun <T, P : Position<P>> Simulation<T, P>.toSimulationStatus(): SimulationStatus = when (state) {
        SimulationState.INIT -> SimulationStatus.INIT
        SimulationState.READY -> SimulationStatus.READY
        SimulationState.PAUSED -> SimulationStatus.PAUSED
        SimulationState.RUNNING -> SimulationStatus.RUNNING
        SimulationState.TERMINATED -> SimulationStatus.TERMINATED
    }
}
