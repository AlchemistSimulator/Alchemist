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
     * The address of the GraphQL server.
     */
    val host: String

    /**
     * The port of the GraphQL server.
     */
    val port: Int

    /**
     * The URL of the GraphQL server.
     */
    fun serverUrl(): String = "http://$host:$port/graphql"

    /**
     * The URL of the GraphQL server subscription.
     */
    fun subscriptionUrl(): String

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
