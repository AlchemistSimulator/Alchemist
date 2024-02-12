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

object NodeStore {

    val nodeStore = createTypedReduxStore(::nodeReducer, NodeState())

    fun nodeById(nodeId: Int = 0) {
        MainScope().launch {
            println("COROUTINE[nodeById]: Started")
            val result = EnvironmentApi.nodeQuery(nodeId).await()

            nodeStore.dispatch(NodeStateAction.SetNode(result))
            println("COROUTINE[nodeById]: Ended")
        }
    }
}
