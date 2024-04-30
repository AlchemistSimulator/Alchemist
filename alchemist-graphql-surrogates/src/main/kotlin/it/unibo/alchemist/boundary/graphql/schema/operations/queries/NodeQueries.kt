/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations.queries

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.EnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NeighborhoodSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.PositionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Set of GraphQL queries to compute on nodes.
 */
class NodeQueries<T, P : Position<out P>>(environment: Environment<T, P>) : Query {

    private val environmentSurrogate: EnvironmentSurrogate<T, P> = environment.toGraphQLEnvironmentSurrogate()

    /**
     * Returns the node position in sapce.
     *
     * @param nodeId the node id
     * @return the position in space of the given node
     */
    @GraphQLDescription("The position in space of the given node.")
    fun nodePosition(nodeId: Int): PositionSurrogate? =
        environmentSurrogate.nodeToPos()[nodeId]

    /**
     * Returns the neighborhood with the provided node as a center.
     *
     * @param nodeId the neighborhood center's node id.
     * @return the neighborhood with this node as a center
     */
    @GraphQLDescription("The neighborhood which the given node is the center.")
    fun neighborhood(nodeId: Int): NeighborhoodSurrogate<T> =
        environmentSurrogate.getNeighborhood(nodeId)
}
