/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.state.reducers

import it.unibo.alchemist.boundary.webui.server.state.actions.SetSimulation
import it.unibo.alchemist.core.Simulation

/**
 * Reducer for the [it.unibo.alchemist.core.Simulation].
 * @param state the current [it.unibo.alchemist.core.Simulation].
 * @param action the requested action.
 * @return the new [it.unibo.alchemist.core.Simulation].
 */
fun simulationReducer(state: Simulation<Any, Nothing>?, action: Any): Simulation<Any, Nothing>? =
    when (action) {
        is SetSimulation -> action.simulation
        else -> state
    }
