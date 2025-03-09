/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.alchemist.boundary.graphql.client.GraphQLClientFactory
import it.unibo.alchemist.boundary.graphql.client.NodesSubscription
import it.unibo.alchemist.boundary.graphql.client.PauseSimulationMutation
import it.unibo.alchemist.boundary.graphql.client.PlaySimulationMutation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimulationState(val status: String = "Loading", val time: Double = 0.0)

class SimulationStatusViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SimulationState())
    val uiState = _uiState.asStateFlow()

    // TODO: parameterize the host and port and separate client in different file
    private val client = GraphQLClientFactory.subscriptionClient(
        "127.0.0.1",
        3000,
    )

    fun pause() {
        viewModelScope.launch {
            client.mutation(PauseSimulationMutation()).execute()
        }
    }

    fun play() {
        viewModelScope.launch {
            client.mutation(PlaySimulationMutation()).execute()
        }
    }

    init {
        viewModelScope.launch {
            client.subscription(NodesSubscription())
                .toFlow()
                .collect { res ->
                    _uiState.update {
                        val data = res.dataOrThrow()
                        SimulationState(
                            status = data.simulation.status,
                            time = data.simulation.time,
                        )
                    }
                }
        }
    }
}
