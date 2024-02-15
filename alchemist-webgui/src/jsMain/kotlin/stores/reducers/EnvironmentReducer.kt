/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.reducers

import stores.actions.EnvironmentStateAction
import stores.states.EnvironmentState

/**
 * Reduces the current state of the environment based on the provided action.
 * @param state The current state of the environment.
 * @param action The action to apply to the state.
 * @return The new state of the environment after applying the action.
 */
fun environmentReducer(state: EnvironmentState, action: EnvironmentStateAction): EnvironmentState {
    when (action) {
        is EnvironmentStateAction.SetNodes -> {
            return state.copy(nodes = action.nodes)
        }
        is EnvironmentStateAction.AddAllNodes -> {
            // Clear the existing nodes and add all nodes from the action
            state.nodes.clear()
            state.nodes.addAll(action.nodes)
            return state
        }
    }
}
