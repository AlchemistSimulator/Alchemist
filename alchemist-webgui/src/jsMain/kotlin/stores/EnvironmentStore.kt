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
import it.unibo.alchemist.boundary.graphql.client.EnvironmentSubscription
import korlibs.io.async.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import stores.actions.EnvironmentStateAction
import stores.reducers.environmentReducer
import stores.states.EnvironmentState

/**
 * Provides access to the Redux store for the environment state.
 */
object EnvironmentStore {

    /**
     * The Redux store for managing environment state, initialized with a default reducer and initial state.
     */
    val store = createTypedReduxStore(::environmentReducer, EnvironmentState(mutableListOf()))
    // private lateinit var job: Job

    /**
     * Asynchronously calls the environment subscription API and dispatches the received data to update the Redux state.
     *
     * This function launches a coroutine on the default dispatcher to perform the asynchronous operation.
     * The environment subscription data is collected from the API using a Flow, and the collected data is then
     * dispatched to update the Redux state by adding all nodes.
     */
    suspend fun callEnvironmentSubscription() {
        async(Dispatchers.Default) {
            EnvironmentApi.environmentSubscription().collect {
                store.dispatch(EnvironmentStateAction.AddAllNodes(it.data?.environment?.nodeToPos!!.entries))
            }
        }
    }

    /**
     * Calls the environment query to fetch node positions asynchronously and dispatches
     * the result to the store.
     */

    // This could be avoided by generalizing the store type
    fun callEnvironmentQuery() {
        CoroutineScope(Dispatchers.Default).launch {
            val result = EnvironmentApi.environmentQuery().await()
                ?.environment
                ?.nodeToPos
                ?.entries!!
                .map { e ->
                    EnvironmentSubscription.Entry(
                        e.id,
                        EnvironmentSubscription.Position(e.position.coordinates),
                    )
                }
            store.dispatch(EnvironmentStateAction.AddAllNodes(result))
        }
    }

    // It seems to degrade performance. Needs further analysis...
    /*fun cancelSubscription(){
        if(::job.isInitialized)
        job.cancel()
    }*/
}
