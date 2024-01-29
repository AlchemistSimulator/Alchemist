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
import stores.actions.EnvironmentStateAction
import stores.reducers.environmentReducer
import stores.states.EnvironmentState

object EnvironmentStore {

    val store = createTypedReduxStore(::environmentReducer, EnvironmentState(mutableListOf()))

    fun callEnvironmentSubscription() {
        MainScope().launch {
            println("Before subscription")
            EnvironmentApi.environMentSubScription().collect { response ->

                store.dispatch(EnvironmentStateAction.AddAllNodes(response.data!!.environment.nodeToPos.entries))
            }
            println("After subscription")
        }
    }
}
