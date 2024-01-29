/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package utils

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
        fun toSimStatus(value: String?): SimState {
            return when (value) {
                "INIT" -> SimState.INIT
                "READY" -> SimState.READY
                "PAUSED" -> SimState.PAUSED
                "RUNNING" -> SimState.RUNNING
                "TERMINATED" -> SimState.TERMINATED
                else -> SimState.TERMINATED // Default to a reasonable value when the string is null or unknown
            }
        }
    }
}
