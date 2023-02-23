/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.api.utility

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import it.unibo.alchemist.common.model.serialization.jsonFormat
import kotlinx.browser.window

/**
 * Client used to make API call to the server.
 */
object JsonClient {

    /**
     * Ktor endpoint. Used by the client to make API calls.
     */
    val endpoint: String = window.location.origin

    /**
     * Http client that will make the API call.
     */
    val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonFormat)
        }
    }
}
