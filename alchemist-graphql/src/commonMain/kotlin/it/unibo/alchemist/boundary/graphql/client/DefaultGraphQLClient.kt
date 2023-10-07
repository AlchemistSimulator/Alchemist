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

/**
 * Default GraphQL client implementation.
 * @param client the Apollo client
 */
class DefaultGraphQLClient(private val client: ApolloClient) : GraphQLClient {
    private var isBuilt: Boolean = false
    private var isSubscriptionModuleEnabled = false

    private constructor(builder: Builder) : this(builder.apolloClientBuilder.build()) {
        isBuilt = true
        isSubscriptionModuleEnabled = builder.isSubscriptionModuleEnabled
    }

    override fun builder(): GraphQLClientBuilder = Builder()

    override fun <D : Query.Data> query(query: Query<D>): ApolloCall<D> {
        checkBuilt()
        return client.query(query)
    }

    override fun <D : Mutation.Data> mutation(mutation: Mutation<D>): ApolloCall<D> {
        checkBuilt()
        return client.mutation(mutation)
    }

    override fun <D : Subscription.Data> subscription(subscription: Subscription<D>): ApolloCall<D> {
        checkBuilt()
        check(isSubscriptionModuleEnabled) { "Subscription module is not enabled!" }
        return client.subscription(subscription)
    }

    override fun close() = client.close()

    private fun checkBuilt() = if (!isBuilt) throw ClientNotBuiltException() else true

    /**
     * [GraphQLClientBuilder] implementation for this [GraphQLClient].
     */
    class Builder : GraphQLClientBuilder {
        private var url: String? = null

        private var isBuilt: Boolean = false
        override var isSubscriptionModuleEnabled: Boolean = false

        /**
         * [ApolloClient.Builder] instance for building the underlying [ApolloClient].
         */
        val apolloClientBuilder = ApolloClient.Builder()

        override fun serverUrl(host: String, port: Int): Builder = apply {
            url = "$host:$port"
            apolloClientBuilder.serverUrl("http://$url/graphql")
        }

        override fun addSubscriptionModule(): Builder = apply {
            check(url == null) { "Server URL must be set before adding subscription module!" }
            apolloClientBuilder.subscriptionNetworkTransport(
                WebSocketNetworkTransport.Builder()
                    .serverUrl("ws://$url/subscriptions")
                    // specifying "graphql-ws" protocol
                    .protocol(GraphQLWsProtocol.Factory())
                    .build(),
            )

            isSubscriptionModuleEnabled = true
        }

        override fun build(): DefaultGraphQLClient {
            isBuilt = true
            return DefaultGraphQLClient(this)
        }
    }
}

/**
 * Exception thrown when the client is not built properly.
 */
class ClientNotBuiltException :
    Exception("The client was not built properly! Make sure to call `build()` before running any operation.")
