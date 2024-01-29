/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package graphql.api

import client.ClientConnection
import it.unibo.alchemist.boundary.graphql.client.PauseSimulationMutation
import it.unibo.alchemist.boundary.graphql.client.PlaySimulationMutation
import it.unibo.alchemist.boundary.graphql.client.SimulationStatusQuery
import it.unibo.alchemist.boundary.graphql.client.TerminateSimulationMutation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object SimulationControlApi {

    suspend fun getSimulationStatus(): Deferred<SimulationStatusQuery.Data?> = coroutineScope {
        async {
            ClientConnection.client.query(SimulationStatusQuery()).execute().data
        }
    }

    suspend fun pauseSimulation() = coroutineScope {
        async {
            withContext(Dispatchers.Default) {
                println("paused the simulation")
                ClientConnection.client.mutation(PauseSimulationMutation()).execute()
            }
        }
    }

    suspend fun playSimulation() = coroutineScope {
        async {
            withContext(Dispatchers.Default) {
                println("playing the simulation")
                ClientConnection.client.mutation(PlaySimulationMutation()).execute()
            }
        }
    }

    suspend fun terminateSimulation() = coroutineScope {
        async {
            withContext(Dispatchers.Default) {
                println("terminated the simulation")
                ClientConnection.client.mutation(TerminateSimulationMutation()).execute()
            }
        }
    }
}
