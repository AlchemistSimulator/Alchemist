/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.client

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.api.Subscription

/**
 * The most basic GraphQL client, capable of executing queries, mutations and subscriptions.
 */
interface GraphQLClient {
    /**
     * Returns a builder for the client.
     */
    fun builder(): GraphQLClientBuilder

    /**
     * Prepare a query to be executed.
     * @param query the query to be executed
     * @return the associated [ApolloCall] that can be executed
     * @see ApolloCall
     */
    fun <D : Query.Data> query(query: Query<D>): ApolloCall<D>

    /**
     * Prepare a mutation to be executed.
     * @param mutation the mutation to be executed
     * @return the associated [ApolloCall] that can be executed
     * @see ApolloCall
     */
    fun <D : Mutation.Data> mutation(mutation: Mutation<D>): ApolloCall<D>

    /**
     * Prepare a subscription to be executed.
     * @param subscription the subscription to be executed
     * @return the associated [ApolloCall] that can be executed
     * @see ApolloCall
     */
    fun <D : Subscription.Data> subscription(subscription: Subscription<D>): ApolloCall<D>

    /**
     * Closes the client.
     */
    fun close(): Unit
}

/**
 *
 * Builds the Apollo GraphQL client with the specified features.
 */
interface GraphQLClientBuilder {
    /**
     * Whether the subscription module is enabled or not.
     */
    var isSubscriptionModuleEnabled: Boolean

    /**
     * Sets the server URL, if nothing is specified, default values will be applied.
     *
     * @param host hostname of the server, default to "localhost"
     * @param port port number of the server, default to "8080"
     */
    fun serverUrl(host: String = "127.0.0.1", port: Int = 8081): GraphQLClientBuilder

    /**
     *
     * Adds WebSocket Network Transport for enabling subscriptions.
     *
     * Note: subscriptions-transport-ws was deprecated in favor of graphql-ws protocol, but
     * because of backward compatibility, Apollo by default uses the deprecated protocol. In
     * this implementation, we explicitly specify the correct version that is now being the HTTP
     * standard.
     */
    fun addSubscriptionModule(): GraphQLClientBuilder

    /**
     * Get the built client.
     */
    fun build(): GraphQLClient
}
