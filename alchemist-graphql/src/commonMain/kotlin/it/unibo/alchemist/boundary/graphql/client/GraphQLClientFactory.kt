/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.client

import it.unibo.alchemist.boundary.graphql.utils.DefaultGraphQLSettings

/**
 * Factory for [GraphQLClient]s.
 */
object GraphQLClientFactory {
    /**
     * Returns a lightweight [GraphQLClient] capable of executing queries and mutations.
     * Note: this client is not capable of executing subscriptions.
     *
     * @param host the host of the GraphQL server
     * @param port the port of the GraphQL server
     */
    fun basicClient(
        host: String = DefaultGraphQLSettings.DEFAULT_HOST,
        port: Int = DefaultGraphQLSettings.DEFAULT_PORT,
    ): GraphQLClient = DefaultGraphQLClient(host, port)

    /**
     * Returns a [GraphQLClient] capable of executing queries, mutations and subscriptions.
     *
     * @param host the host of the GraphQL server
     * @param port the port of the GraphQL server
     */
    fun subscriptionClient(
        host: String = DefaultGraphQLSettings.DEFAULT_HOST,
        port: Int = DefaultGraphQLSettings.DEFAULT_PORT,
    ): GraphQLClient = DefaultGraphQLClient(host, port, enableSubscription = true)
}
