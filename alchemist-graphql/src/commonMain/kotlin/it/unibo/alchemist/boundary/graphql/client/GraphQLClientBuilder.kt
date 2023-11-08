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
     * @param host hostname of the server, default to localhost "127.0.0.1"
     * @param port port number of the server, default to "8081"
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
