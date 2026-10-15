/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.surrogates.utility

import it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate
import it.unibo.alchemist.core.SimulationState

/**
 * Map a [SimulationState] to [StatusSurrogate].
 */
fun SimulationState.toStatusSurrogate(): StatusSurrogate = when (this) {
    SimulationState.INIT -> StatusSurrogate.INIT
    SimulationState.READY -> StatusSurrogate.READY
    SimulationState.PAUSED -> StatusSurrogate.PAUSED
    SimulationState.RUNNING -> StatusSurrogate.RUNNING
    SimulationState.TERMINATED -> StatusSurrogate.TERMINATED
}
