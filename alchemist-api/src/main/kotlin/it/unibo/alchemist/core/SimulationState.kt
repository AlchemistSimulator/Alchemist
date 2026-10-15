/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

/**
 * The possible states of a [Simulation].
 * The order of the entries follows the simulation lifecycle, and [isReachableFrom] relies on it.
 */
enum class SimulationState {
    /**
     * The simulation is being initialized.
     */
    INIT,

    /**
     * The simulation is ready to be started.
     */
    READY,

    /**
     * The simulation is paused. It can be resumed.
     */
    PAUSED,

    /**
     * The simulation is currently running.
     */
    RUNNING,

    /**
     * The simulation is stopped. It is no longer possible to resume it.
     */
    TERMINATED,
    ;

    /**
     * Whether the simulation lifecycle allows getting to this state from [origin].
     */
    fun isReachableFrom(origin: SimulationState): Boolean = this >= origin || this == PAUSED && origin == RUNNING
}
