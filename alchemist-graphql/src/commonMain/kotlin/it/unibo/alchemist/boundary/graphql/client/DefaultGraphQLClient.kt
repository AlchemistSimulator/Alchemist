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
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.api.Subscription
import com.apollographql.apollo3.network.ws.GraphQLWsProtocol
import com.apollographql.apollo3.network.ws.WebSocketNetworkTransport
import it.unibo.alchemist.boundary.graphql.utils.DefaultGraphQLSettings

/**
 * Default GraphQL client implementation.
 *
 * @param host the host of the GraphQL server
 * @param port the port of the GraphQL server
 * @param enableSubscription whether to enable subscriptions or not
 */
data class DefaultGraphQLClient(
    override val host: String = DefaultGraphQLSettings.DEFAULT_HOST,
    override val port: Int = DefaultGraphQLSettings.DEFAULT_PORT,
    private val enableSubscription: Boolean = false,
) : GraphQLClient {

    private val client: ApolloClient = ApolloClient.Builder()
        .serverUrl(serverUrl())
        .apply {
            if (enableSubscription) {
                subscriptionNetworkTransport(
                    WebSocketNetworkTransport.Builder()
                        .serverUrl(subscriptionUrl())
                        .protocol(GraphQLWsProtocol.Factory())
                        .build(),
                )
            }
        }
        .build()

    override fun subscriptionUrl(): String {
        check(enableSubscription) { "Subscription module is not enabled!" }
        return "ws://$host:$port/subscriptions"
    }

    override fun <D : Query.Data> query(query: Query<D>): ApolloCall<D> {
        return client.query(query)
    }

    override fun <D : Mutation.Data> mutation(mutation: Mutation<D>): ApolloCall<D> {
        return client.mutation(mutation)
    }

    override fun <D : Subscription.Data> subscription(subscription: Subscription<D>): ApolloCall<D> {
        check(enableSubscription) { "Subscription module is not enabled!" }
        return client.subscription(subscription)
    }

    override fun close() = client.close()
}
