/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.routes

import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import it.unibo.alchemist.boundary.webui.common.utility.Routes.SIMULATION_PAUSE_PATH
import it.unibo.alchemist.boundary.webui.common.utility.Routes.SIMULATION_PLAY_PATH
import it.unibo.alchemist.boundary.webui.common.utility.Routes.SIMULATION_STATUS_PATH
import it.unibo.alchemist.boundary.webui.server.state.ServerStore.store
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.toStatusSurrogate
import it.unibo.alchemist.boundary.webui.server.utility.Response
import it.unibo.alchemist.boundary.webui.server.utility.Response.Companion.respond
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status

/**
 * Logic of the Routes in the /simulation path.
 */
object SimulationRoute {
    /**
     * Route that retrieve the simulation status and return it to the client mapping it with the [toStatusSurrogate]
     * function.
     * The HTTP [Response] sent to the client can be of type:
     * 200 (OK) if the status is correctly retrieved;
     * 500 (Server Error) if the Simulation was not loaded correctly.
     */
    fun Route.simulationStatus() {
        get(SIMULATION_STATUS_PATH) {
            store.state.simulation?.status?.toStatusSurrogate()?.let { status ->
                respond(Response(OK, status))
            } ?: respond(Response(InternalServerError, "Simulation not loaded."))
        }
    }

    /**
     * Route that plays the simulation by calling the [it.unibo.alchemist.core.Simulation.play] method.
     * The HTTP [Response] sent to the client can be of type:
     * 200 (OK) if the operation succeed;
     * 409 (Conflict) if the simulation is already running, or is in an invalid state to call the play operation;
     * 500 (Server Error) if the Simulation was not loaded correctly.
     */
    fun Route.simulationActionPlay() {
        post(SIMULATION_PLAY_PATH) {
            respondAction(Pair(Status.RUNNING, "The Simulation is already running.")) { simulation ->
                simulation.play()
            }
        }
    }

    /**
     * Route that pauses the simulation by calling the [it.unibo.alchemist.core.Simulation.pause] method.
     * The HTTP [Response] sent to the client can be of type:
     * 200 (OK) if the operation succeed;
     * 409 (Conflict) if the simulation is already paused, or is in an invalid state to call the pause operation;
     * 500 (Server Error) if the Simulation was not loaded correctly.
     */
    fun Route.simulationActionPause() {
        post(SIMULATION_PAUSE_PATH) {
            respondAction(Pair(Status.PAUSED, "The Simulation is already paused.")) { simulation ->
                simulation.pause()
            }
        }
    }

    /**
     * Private function that checks the simulation Status, executes the requested action if possible and responds.
     * @param additionalCheck a [Pair] representing an additional Status check, first is an invalid [Status] and second
     * is the error message.
     * @param action the action to execute on the simulation.
     */
    private suspend fun RoutingContext.respondAction(
        additionalCheck: Pair<Status, String>,
        action: (Simulation<Any, Nothing>) -> Unit,
    ) {
        store.state.simulation?.let { simulation ->
            respond(
                when (simulation.status) {
                    Status.INIT -> Response(Conflict, "The Simulation is being initialized.")
                    Status.TERMINATED -> Response(Conflict, "The Simulation is terminated.")
                    additionalCheck.first -> Response(Conflict, additionalCheck.second)
                    else -> {
                        action(simulation)
                        Response(OK, "Action executed.")
                    }
                },
            )
        } ?: respond(Response(InternalServerError, "No Simulation found on the server."))
    }
}
