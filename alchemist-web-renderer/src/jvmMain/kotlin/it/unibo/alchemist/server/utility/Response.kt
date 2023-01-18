/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.utility

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.ktor.http.HttpStatusCode.Companion.OK

/**
 * Class representing an HTTP response.
 * @param code the [HttpStatusCode].
 * @param content the content of the message.
 */
data class Response<C>(
    val code: HttpStatusCode = OK,
    val content: C
) {
    companion object {
        /**
         * Utility function to dry-run the response process.
         */
        suspend inline fun <reified C : Any> PipelineContext<Unit, ApplicationCall>.respond(response: Response<C>) {
            call.respond(response.code, response.content)
        }
    }
}
