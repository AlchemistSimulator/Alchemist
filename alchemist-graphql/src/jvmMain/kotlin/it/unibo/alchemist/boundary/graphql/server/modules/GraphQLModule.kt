/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.server.modules

import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import com.expediagroup.graphql.server.ktor.DefaultKtorGraphQLContextFactory
import com.expediagroup.graphql.server.ktor.GraphQL
import io.ktor.serialization.jackson.JacksonWebsocketContentConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import it.unibo.alchemist.boundary.graphql.schema.operations.mutations.SimulationHandler
import it.unibo.alchemist.boundary.graphql.schema.operations.queries.EnvironmentQueries
import it.unibo.alchemist.boundary.graphql.schema.operations.subscriptions.EnvironmentSubscriptions
import it.unibo.alchemist.boundary.graphql.server.attributes.SimulationAttributeKey
import java.time.Duration

// The following values are referred to milliseconds.
private const val DEFAULT_PING_PERIOD = 1000L
private const val DEFAULT_TIMEOUT_DURATION = 10000L

/**
 * Ktor module for enabling GraphQL on server.
 */
fun Application.graphQLModule() {
    install(CORS) {
        anyHost()
    }

    install(Compression) {
        gzip()
    }

    install(WebSockets) {
        pingPeriod = Duration.ofMillis(DEFAULT_PING_PERIOD)
        timeout = Duration.ofMillis(DEFAULT_TIMEOUT_DURATION)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = JacksonWebsocketContentConverter()
    }

    val environment = attributes[SimulationAttributeKey].environment

    install(GraphQL) {
        schema {
            packages = listOf(
                "it.unibo.alchemist.boundary.graphql.schema",
            )
            queries = listOf(
                EnvironmentQueries(environment),
            )
            mutations = listOf(
                SimulationHandler(environment),
            )
            subscriptions = listOf(
                EnvironmentSubscriptions(environment),
            )
            hooks = FlowSubscriptionSchemaGeneratorHooks()
        }

        server {
            contextFactory = DefaultKtorGraphQLContextFactory()
        }
    }
}
