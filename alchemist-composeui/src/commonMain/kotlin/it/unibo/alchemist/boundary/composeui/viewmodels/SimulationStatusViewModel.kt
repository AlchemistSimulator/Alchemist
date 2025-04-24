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
import com.apollographql.apollo3.api.Error
import it.unibo.alchemist.boundary.graphql.client.GraphQLClientFactory
import it.unibo.alchemist.boundary.graphql.client.NodesSubscription
import it.unibo.alchemist.boundary.graphql.client.PauseSimulationMutation
import it.unibo.alchemist.boundary.graphql.client.PlaySimulationMutation
import it.unibo.alchemist.boundary.graphql.client.SimulationStatusQuery
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SimulationStatus {
    Init,
    Ready,
    Paused,
    Running,
    Terminated,
}

var i = 0
var j = 0

class SimulationStatusViewModel : ViewModel() {
    private val _simulationStatus = MutableStateFlow(SimulationStatus.Init)
    val simulationStatus = _simulationStatus.asStateFlow()

    private val _time = MutableStateFlow(0.0)
    val time = _time.asStateFlow()
    private var tempTime = 0.0

    private val _errors = MutableStateFlow<List<Error>>(emptyList())
    val errors = _errors.asStateFlow()

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

    fun monitor() {
        _errors.value = emptyList()
        viewModelScope.launch {
            client.subscription(NodesSubscription())
                .toFlow()
                .collect { response ->
                    if (response.hasErrors()) {
                        response.errors?.let { errors ->
                            _errors.update { errors }
                        }
                    }
                    i++
                    println("data received $i")
                    if (i > 99) {
                        i = 0
                        j++
                        println("fetching data $j")
                        response.data?.let { data ->
                            _time.value = data.simulation.time
                        }
                    }
                }
        }
    }

    init {
        monitor()
        viewModelScope.launch {
            while (true) {
                client.query(SimulationStatusQuery())
                    .toFlow()
                    .collect { response ->
                        response.data?.let { data ->
                            _simulationStatus.update {
                                // True correlation can be achieved only moving
                                // alchemist-api Status enum class to commonMain
                                when (data.simulation.status) {
                                    "READY" -> SimulationStatus.Ready
                                    "PAUSED" -> SimulationStatus.Paused
                                    "RUNNING" -> SimulationStatus.Running
                                    "TERMINATED" -> SimulationStatus.Terminated
                                    else -> SimulationStatus.Init
                                }
                            }
                        }
                    }
                delay(50)
            }
        }
    }
}
