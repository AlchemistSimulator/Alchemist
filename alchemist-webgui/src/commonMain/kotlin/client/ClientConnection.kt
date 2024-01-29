/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package client

import it.unibo.alchemist.boundary.graphql.client.GraphQLClient
import it.unibo.alchemist.boundary.graphql.client.GraphQLClientFactory

object ClientConnection {

    val client: GraphQLClient = GraphQLClientFactory.subscriptionClient()
}
