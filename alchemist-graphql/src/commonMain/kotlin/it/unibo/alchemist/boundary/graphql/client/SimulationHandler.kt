/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.client

import com.apollographql.apollo3.api.Mutation

/**
 * Utility class to handle simulation lifecycle through GraphQL queries.
 *
 * @param graphqlClient the GraphQL client connected to the server
 */
class SimulationHandler(private val graphqlClient: GraphQLClient) {

    /**
     * Pauses the simulation.
     */
    suspend fun pause() =
        handleSimulation(
            PauseSimulationMutation(),
            "PAUSING SIMULATION",
        ) as PauseSimulationMutation.Data

    /**
     * Terminates the simulation.
     */
    suspend fun terminate() =
        handleSimulation(
            TerminateSimulationMutation(),
            "TERMINATING SIMULATION",
        ) as TerminateSimulationMutation.Data

    /**
     * Plays the simulation.
     */
    suspend fun play(): PlaySimulationMutation.Data {
        val status = handleSimulation(
            PlaySimulationMutation(),
            "STARTING SIMULATION",
        ) as PlaySimulationMutation.Data

        require(status.play == "RUNNING") { status.play }
        return status
    }

    /**
     * Returns the status of the simulation.
     */
    suspend fun status() = graphqlClient.query(SimulationStatusQuery()).execute().data?.simulationStatus

    private suspend fun handleSimulation(action: Mutation<*>, message: String) =
        graphqlClient.mutation(action).execute().also { println(message) }.data!!
}
