/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations.subscriptions

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Subscription
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NeighborhoodSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NodeSurrogate
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.util.Environments.subscriptionMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Exposes alchemist [it.unibo.alchemist.model.Node]s as GraphQL subscriptions.
 *
 * @param environment the environment.
 */
class NodeSubscriptions<T, P : Position<out P>>(environment: Environment<T, P>) : Subscription {
    private val environmentMonitor = environment.subscriptionMonitor()

    /**
     * Returns a [Flow] with the updated value of the
     * [it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NodeSurrogate] with the given Id.
     * @param nodeId the node Id.
     */
    @GraphQLDescription("A node in the simulation's environment")
    fun node(nodeId: Int): Flow<NodeSurrogate<T>> = environmentMonitor.eventFlow.map { it.nodeById(nodeId) }

    /**
     * Returns a [Flow] with the updated value of the
     * [it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NeighborhoodSurrogate] with the given node
     * as a part of the neighborhood.
     *
     * @param nodeId the node Id.
     * @return the neighborhood of the node with the given Id.
     */
    @GraphQLDescription("The neighborhood of a node in the simulation's environment")
    fun neighborhood(nodeId: Int): Flow<NeighborhoodSurrogate<T>> =
        environmentMonitor.eventFlow.map { it.getNeighborhood(nodeId) }
}
