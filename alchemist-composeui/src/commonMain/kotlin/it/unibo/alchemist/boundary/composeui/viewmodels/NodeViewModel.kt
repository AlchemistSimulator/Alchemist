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
import it.unibo.alchemist.boundary.graphql.client.NodeInfoSubscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Molecule(val name: String)

data class MoleculeConcentration(val concentration: String, val molecule: Molecule)

data class NodeInfo(
    val id: Int,
    val moleculeCount: Int,
    val properties: List<String>,
    val contents: List<MoleculeConcentration>,
)

class NodeViewModel(private val nodeId: Int) : ViewModel() {
    private val _nodeInfo = MutableStateFlow<NodeInfo?>(null)
    val nodeInfo = _nodeInfo.asStateFlow()

    private val _errors = MutableStateFlow<List<Error>>(emptyList())
    val errors = _errors.asStateFlow()

    // TODO: parameterize the host and port and separate client in different file
    private val client = GraphQLClientFactory.subscriptionClient(
        "127.0.0.1",
        3000,
    )

    private fun load() {
        _errors.value = emptyList()
        viewModelScope.launch {
            client.subscription(NodeInfoSubscription(nodeId))
                .toFlow()
                .collect { response ->
                    response.data?.let { data ->
                        _nodeInfo.value = NodeInfo(
                            data.environment.nodeById.id,
                            data.environment.nodeById.moleculeCount,
                            data.environment.nodeById.properties,
                            data.environment.nodeById.contents.entries.map {
                                MoleculeConcentration(
                                    it.concentration,
                                    Molecule(it.molecule.name),
                                )
                            },
                        )
                    }
                }
        }
    }

    init {
        load()
    }
}
