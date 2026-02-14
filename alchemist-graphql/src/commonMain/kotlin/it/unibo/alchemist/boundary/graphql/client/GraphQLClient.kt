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
 * Basic GraphQL client capable of preparing queries, mutations, and subscriptions for execution.
 */
interface GraphQLClient {
    /** The address of the GraphQL server. */
    val host: String

    /** The port of the GraphQL server. */
    val port: Int

    /**
     * Returns the HTTP URL of the GraphQL endpoint.
     *
     * @return the HTTP server URL where GraphQL requests should be sent.
     */
    fun serverUrl(): String = "http://$host:$port/graphql"

    /**
     * Returns the URL to use for GraphQL subscriptions (often a WebSocket URL).
     *
     * @return the subscription URL as a [String].
     */
    fun subscriptionUrl(): String

    /**
     * Prepare a query to be executed.
     *
     * @param D the query data type produced by the query.
     * @param query the query to be executed.
     * @return an [ApolloCall] that can be executed to perform the query.
     * @see ApolloCall
     */
    fun <D : Query.Data> query(query: Query<D>): ApolloCall<D>

    /**
     * Prepare a mutation to be executed.
     *
     * @param D the mutation data type produced by the mutation.
     * @param mutation the mutation to be executed.
     * @return an [ApolloCall] that can be executed to perform the mutation.
     * @see ApolloCall
     */
    fun <D : Mutation.Data> mutation(mutation: Mutation<D>): ApolloCall<D>

    /**
     * Prepare a subscription to be executed.
     *
     * @param D the subscription data type produced by the subscription.
     * @param subscription the subscription to be executed.
     * @return an [ApolloCall] that can be executed to perform the subscription.
     * @see ApolloCall
     */
    fun <D : Subscription.Data> subscription(subscription: Subscription<D>): ApolloCall<D>

    /**
     * Closes any resources held by the client.
     */
    fun close()
}
