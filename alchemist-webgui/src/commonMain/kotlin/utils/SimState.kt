/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package utils

/**
 * Represents the possible states of a simulation. Intermediate enum for the simulation status.
 *
 */
enum class SimState {
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
     * The simulation is stopped. It is no longer possible to resume
     * it.
     */
    TERMINATED, ;

    companion object {

        /**
         * Converts a string representation of a simulation status to the corresponding SimState enum value.
         *
         * @param value the string representation of the simulation status
         * @return the SimState enum value corresponding to the input string, or [SimState.TERMINATED] if the input is
         * invalid.
         */
        fun toSimStatus(value: String?): SimState {
            return when (value) {
                "INIT" -> INIT
                "READY" -> READY
                "PAUSED" -> PAUSED
                "RUNNING" -> RUNNING
                "TERMINATED" -> TERMINATED
                else -> TERMINATED
            }
        }
    }
}
