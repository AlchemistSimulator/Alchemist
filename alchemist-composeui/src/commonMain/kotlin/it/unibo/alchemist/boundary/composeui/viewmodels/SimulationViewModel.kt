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
import it.unibo.alchemist.boundary.graphql.client.PauseSimulationMutation
import it.unibo.alchemist.boundary.graphql.client.PlaySimulationMutation
import it.unibo.alchemist.boundary.graphql.client.SimulationSubscription
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

data class Node(val id: Int, val coordinates: List<Double>)

class SimulationViewModel : ViewModel() {
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    private val _status = MutableStateFlow(SimulationStatus.Init)
    private val _errors = MutableStateFlow<List<Error>>(emptyList())

    val nodes = _nodes.asStateFlow()
    val status = _status.asStateFlow()
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

    fun fetch() {
        _errors.value = emptyList()
        viewModelScope.launch {
            client.subscription(SimulationSubscription())
                .toFlow()
                .collect { response ->
                    if (response.hasErrors()) {
                        response.errors?.let { errors ->
                            _errors.update { errors }
                        }
                    }
                    response.data?.let { data ->
                        _status.update {
                            when (data.simulation.status) {
                                "READY" -> SimulationStatus.Ready
                                "PAUSED" -> SimulationStatus.Paused
                                "RUNNING" -> SimulationStatus.Running
                                "TERMINATED" -> SimulationStatus.Terminated
                                else -> SimulationStatus.Init
                            }
                        }
                        _nodes.value = data.simulation.environment.nodeToPos.entries.map {
                            Node(
                                id = it.id,
                                coordinates = it.position.coordinates,
                            )
                        }
                    }
                }
        }
    }

    init {
        fetch()
    }
}
