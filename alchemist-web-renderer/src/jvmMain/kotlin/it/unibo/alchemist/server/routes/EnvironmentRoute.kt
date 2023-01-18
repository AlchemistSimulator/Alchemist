/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import it.unibo.alchemist.common.model.serialization.encodeEnvironmentSurrogate
import it.unibo.alchemist.common.model.serialization.jsonFormat
import it.unibo.alchemist.common.renderer.Bitmap32Serializer
import it.unibo.alchemist.common.utility.Routes.environmentClientPath
import it.unibo.alchemist.common.utility.Routes.environmentServerPath
import it.unibo.alchemist.server.state.ServerStore.store
import it.unibo.alchemist.server.utility.Response
import it.unibo.alchemist.server.utility.Response.Companion.respond
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Logic of the Routes in the /environment path.
 */
object EnvironmentRoute {

    /**
     * Route of type GET that retrieve current Environment.
     * The server will render the environment and send it to the client in an already rendered form.
     * The HTTP [Response] sent to the client can be of type:
     * 200 (OK) the Environment is sent to the client.
     */
    fun Route.environmentServerMode() {
        get(environmentServerPath) {
            respond(Response(content = renderedEnvironment()))
        }
    }

    /**
     * Route of type GET that retrieve current Environment.
     * The server will send the environment to the client in a serialized form.
     * The HTTP [Response] sent to the client can be of type:
     * 200 (OK) the Environment is sent to the client.
     */
    fun Route.environmentClientMode() {
        get(environmentClientPath) {
            respond(Response(content = jsonFormat.encodeEnvironmentSurrogate(store.state.environmentSurrogate)))
        }
    }

    private suspend fun renderedEnvironment(
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): String {
        return withContext(dispatcher) {
            jsonFormat.encodeToString(
                Bitmap32Serializer,
                store.state.renderer.render(
                    store.state.environmentSurrogate
                ).toBMP32IfRequired()
            )
        }
    }
}
