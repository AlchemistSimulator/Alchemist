/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.api
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import it.unibo.alchemist.boundary.webui.client.api.utility.JsonClient.client
import it.unibo.alchemist.boundary.webui.client.api.utility.JsonClient.endpoint
import it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate
import it.unibo.alchemist.boundary.webui.common.utility.Routes

/**
 * API to interact with the simulation using the Play Button.
 */
object SimulationApi {

    /**
     * Get the simulation status as a [StatusSurrogate].
     */
    suspend fun getSimulationStatus(): StatusSurrogate {
        return client.get(endpoint + Routes.simulationStatusPath).body()
    }

    /**
     * Plays the simulation.
     */
    suspend fun playSimulation(): HttpResponse {
        return client.post(endpoint + Routes.simulationPlayPath)
    }

    /**
     * Pauses the simulation.
     */
    suspend fun pauseSimulation(): HttpResponse {
        return client.post(endpoint + Routes.simulationPausePath)
    }
}
