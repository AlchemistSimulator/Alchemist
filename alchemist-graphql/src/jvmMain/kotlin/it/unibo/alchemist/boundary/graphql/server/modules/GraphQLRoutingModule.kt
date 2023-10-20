/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.server.modules

import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphQLSubscriptionsRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

/**
 * Ktor module that configure needed GraphQL routes, some of which are
 * the main entrypoint "/graphql", subscription entrypoint "/subscriptions"
 * and Schema Definition Language Route.
 */
fun Application.graphQLRoutingModule() {
    routing {
        graphQLGetRoute()
        graphQLSubscriptionsRoute()
        graphQLPostRoute()
        graphQLSDLRoute()
        graphiQLRoute()
    }
}
