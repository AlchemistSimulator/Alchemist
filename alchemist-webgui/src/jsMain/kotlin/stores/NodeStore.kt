/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores

import graphql.api.EnvironmentApi
import io.kvision.redux.createTypedReduxStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import stores.actions.NodeStateAction
import stores.reducers.nodeReducer
import stores.states.NodeState

/**
 * Provides access to the Redux store for the selected node state.
 */
object NodeStore {

    /**
     * The Redux store for managing node state, initialized with a default reducer and initial state.
     */
    val nodeStore = createTypedReduxStore(::nodeReducer, NodeState())

    /**
     * Retrieves node data by ID and updates the node state in the store asynchronously.
     * @param nodeId The ID of the node to retrieve. Defaults to 0 if not specified.
     */
    fun nodeById(nodeId: Int = 0) {
        MainScope().launch {
            val result = EnvironmentApi.nodeQuery(nodeId).await()

            // Dispatches an action to update the node state in the store with the retrieved data.
            nodeStore.dispatch(NodeStateAction.SetNode(result))
        }
    }
}
