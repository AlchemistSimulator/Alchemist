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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object SimulationControlApi {

    /**
     * Asynchronously queries the status of the simulation from the server.
     *
     * @return a Deferred object representing the asynchronous result of the simulation status query
     */
    suspend fun getSimulationStatus(): Deferred<SimulationStatusQuery.Data?> = coroutineScope {
        async {
            ClientConnection.client.query(SimulationStatusQuery()).execute().data
        }
    }

    /**
     * Asynchronously pauses the simulation on the server.
     */
    suspend fun pauseSimulation(): PauseSimulationMutation.Data? {
        return ClientConnection.client.mutation(PauseSimulationMutation()).execute().data
    }

    /**
     * Asynchronously resumes or starts the simulation on the server.
     *
     * @return the data object containing the result of the play simulation mutation
     */
    suspend fun playSimulation(): PlaySimulationMutation.Data? {
        return ClientConnection.client.mutation(PlaySimulationMutation()).execute().data
    }

    /**
     * Asynchronously terminates the simulation on the server.
     */
    suspend fun terminateSimulation(): TerminateSimulationMutation.Data? {
        return ClientConnection.client.mutation(TerminateSimulationMutation()).execute().data
    }
}
