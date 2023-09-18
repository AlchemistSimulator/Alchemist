/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model.surrogates

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.boundary.graphql.schema.util.NodeToPosMap
import it.unibo.alchemist.boundary.graphql.schema.util.toNodeToPosMap
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * A surrogate for [Environment].
 * @param T the concentration type
 * @param P the position
 * @param dimensions the number of dimensions of this environment.
 */
@GraphQLDescription("The simulation environment")
data class EnvironmentSurrogate<T, P : Position<P>>(
    @GraphQLIgnore override val origin: Environment<T, P>,
    val dimensions: Int = origin.dimensions,
) : GraphQLSurrogate<Environment<T, P>>(origin) {
    /**
     * The nodes inside this environment.
     * @return the nodes in this environment.
     */
    fun nodes() = origin.nodes.map { NodeSurrogate(it) }

    /**
     * Returns the node with the given id.
     * @param id the id of the node
     */
    fun nodeById(id: Int): NodeSurrogate<T> = origin.getNodeByID(id).toGraphQLNodeSurrogate()

    /**
     * Returns a [NodeToPosMap] representing all nodes associated with their position.
     */
    fun nodeToPos(): NodeToPosMap = origin.nodes.associate { it.id to origin.getPosition(it) }.toNodeToPosMap()

    /**
     * Returns the neighborhood of the node with the given id.
     *
     * @param nodeId the id of the node
     * @return the neighborhood of the node with the given id.
     */
    fun getNeighborhood(nodeId: Int): NeighborhoodSurrogate<T> =
        origin.getNeighborhood(origin.getNodeByID(nodeId)).toGraphQLNeighborhoodSurrogate()
}

/**
 * Converts an [Environment] to a [EnvironmentSurrogate].
 * @param T the concentration type
 * @param P the position
 * @return a [EnvironmentSurrogate] representing the given [Environment]
 */
fun <T, P : Position<P>> Environment<T, P>.toGraphQLEnvironmentSurrogate() = EnvironmentSurrogate(this)
