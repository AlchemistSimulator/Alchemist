/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.state

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.TestUtility.webRendererTestEnvironments
import it.unibo.alchemist.common.model.surrogate.EnvironmentSurrogate.Companion.uninitializedEnvironment
import it.unibo.alchemist.server.state.actions.SetEnvironmentSurrogate
import it.unibo.alchemist.server.state.actions.SetSimulation
import it.unibo.alchemist.server.surrogates.utility.ToConcentrationSurrogate.toEmptyConcentration
import it.unibo.alchemist.server.surrogates.utility.toEnvironmentSurrogate
import org.reduxkotlin.Store
import org.reduxkotlin.threadsafe.createThreadSafeStore

class ServerStoreTest : StringSpec({

    val serverStore: Store<ServerState> = createThreadSafeStore(::rootReducer, ServerState())

    "ServerStore should be empty at the beginning" {
        serverStore.state.simulation shouldBe null
        serverStore.state.environmentSurrogate shouldBe uninitializedEnvironment()
    }

    "ServerState can be updated with a SetSimulation action" {
        webRendererTestEnvironments<Any, Nothing>().forEach {
            serverStore.dispatch(SetSimulation(it.simulation))
            serverStore.state.simulation shouldBe it.simulation
        }
    }

    "ServerStore can be updated with a SetEnvironmentSurrogate action" {
        webRendererTestEnvironments<Any, Nothing>().forEach {
            val envSurrogate = it.toEnvironmentSurrogate(toEmptyConcentration)
            serverStore.dispatch(SetEnvironmentSurrogate(envSurrogate))
            serverStore.state.environmentSurrogate shouldBe envSurrogate
        }
    }
})
