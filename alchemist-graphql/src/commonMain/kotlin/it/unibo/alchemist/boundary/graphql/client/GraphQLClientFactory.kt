/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.client

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
        host: String = "127.0.0.1",
        port: Int = 8081,
    ): GraphQLClient = DefaultGraphQLClient.Builder().serverUrl(host, port).build()

    /**
     * Returns a [GraphQLClient] capable of executing queries, mutations and subscriptions.
     *
     * @param host the host of the GraphQL server
     * @param port the port of the GraphQL server
     */
    fun subscriptionClient(
        host: String = "127.0.0.1",
        port: Int = 8081,
    ): GraphQLClient = DefaultGraphQLClient.Builder().serverUrl(host, port).addSubscriptionModule().build()
}
