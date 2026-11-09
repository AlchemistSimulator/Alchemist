/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

/**
 * High-level simulation status mirrored in the Compose UI.
 */
enum class SimulationStatus {
    /** Initializing. */
    INIT,

    /** Ready to start. */
    READY,

    /** Paused. */
    PAUSED,

    /** Running. */
    RUNNING,

    /** Finished; cannot be resumed. */
    TERMINATED,
}
