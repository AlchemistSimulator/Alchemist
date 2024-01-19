/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/*import com.apollographql.apollo3.api.Optional
import it.unibo.alchemist.boundary.graphql.client.GraphQLClient
import it.unibo.alchemist.boundary.graphql.client.GraphQLClientFactory
import it.unibo.alchemist.boundary.graphql.client.EnvironmentSubscription
import it.unibo.alchemist.boundary.graphql.client.GraphQLClient
import it.unibo.alchemist.boundary.graphql.client.GraphQLClientFactory
import it.unibo.alchemist.boundary.graphql.client.NodeQuery
import it.unibo.alchemist.boundary.webui.common.model.surrogate.Position2DSurrogate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first*/

open class ClientConnection() {

    // private val client: GraphQLClient = GraphQLClientFactory.subscriptionClient()

    // private val pos: Position2DSurrogate = Position2DSurrogate(5.0,6.0)

    /*suspend fun retrieveQuery(): Deferred<NodeQuery.Data?> = coroutineScope {
        async {
            client.query(NodeQuery(id = Optional.present(10))).execute().data
        }
    }

    suspend fun environMentSubScription(): Deferred<EnvironmentSubscription.Data?> = coroutineScope {
        async {
            client.subscription(EnvironmentSubscription()).toFlow().first().data
        }
    }*/
}
