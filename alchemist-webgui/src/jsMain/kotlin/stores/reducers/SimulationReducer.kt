/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.reducers

import stores.actions.SimulationAction
import stores.states.SimulationState

fun simulationReducer(state: SimulationState, action: SimulationAction): SimulationState = when (action) {
    is SimulationAction.SetSimulation -> {
        state.copy(status = action.simulationState)
    }
}
