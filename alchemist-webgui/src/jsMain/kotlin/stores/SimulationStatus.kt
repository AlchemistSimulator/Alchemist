/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores

import graphql.api.SimulationControlApi
import io.kvision.redux.createTypedReduxStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import stores.actions.SimulationAction
import stores.reducers.simulationReducer
import stores.states.SimulationState

/**
 * Provides access to the Redux store for simulation status-related state management.
 */
object SimulationStatus {

    /**
     * The Redux store for managing simulation status state, initialized with a default reducer and initial state.
     */
    val simulationStore = createTypedReduxStore(::simulationReducer, SimulationState())

    /**
     * Asynchronously retrieves the simulation status and updates the simulation state in the store.
     * Dispatches an action to update the simulation state in the store with the retrieved status data.
     */
    fun callGetStatus() {
        MainScope().launch {
            val result = SimulationControlApi.getSimulationStatus().await()

            simulationStore.dispatch(SimulationAction.SetSimulation(result))
        }
    }
}
