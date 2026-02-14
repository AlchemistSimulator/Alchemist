/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.client

import com.apollographql.apollo3.api.Mutation

/**
 * Utility to control simulation lifecycle through GraphQL operations.
 *
 * @param graphqlClient the GraphQL client connected to the server.
 */
class SimulationHandler(private val graphqlClient: GraphQLClient) {
    /** Pauses the simulation and returns the mutation data. */
    suspend fun pause() = handleSimulation(
        PauseSimulationMutation(),
    ) as PauseSimulationMutation.Data

    /** Terminates the simulation and returns the mutation data. */
    suspend fun terminate() = handleSimulation(
        TerminateSimulationMutation(),
    ) as TerminateSimulationMutation.Data

    /**
     * Starts or resumes the simulation and returns the mutation data. The method checks that the
     * returned status is "RUNNING" and throws an exception otherwise.
     *
     * @return the [PlaySimulationMutation.Data] containing the simulation status.
     */
    suspend fun play(): PlaySimulationMutation.Data {
        val status =
            handleSimulation(
                PlaySimulationMutation(),
            ) as PlaySimulationMutation.Data

        check(status.play == "RUNNING") { status.play }
        return status
    }

    /**
     * Returns the current simulation status string (if available) or null.
     *
     * @return the simulation status string or null if unavailable.
     */
    suspend fun status() = graphqlClient
        .query(SimulationStatusQuery())
        .execute()
        .data
        ?.simulation
        ?.status

    private suspend fun handleSimulation(action: Mutation<*>) = checkNotNull(
        graphqlClient.mutation(action).execute().data,
    )
}
