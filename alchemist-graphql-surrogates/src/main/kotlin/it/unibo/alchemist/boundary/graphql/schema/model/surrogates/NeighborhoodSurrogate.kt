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
import it.unibo.alchemist.model.Neighborhood

/**
 * A GraphQL surrogate for a [Neighborhood].
 */
@GraphQLDescription("A neighborhood of nodes")
data class NeighborhoodSurrogate<T>(
    @GraphQLIgnore override val origin: Neighborhood<T>,
) : GraphQLSurrogate<Neighborhood<T>>(origin) {

    /**
     * @return the size of this neighborhood.
     */
    val size: Int
        get() = origin.size()

    /**
     * @return the central node of this neighborhood.
     */
    @GraphQLDescription("The central node of this neighborhood")
    fun getCenter(): NodeSurrogate<T> = origin.center.toGraphQLNodeSurrogate()

    /**
     * @return the list of the neighbors.
     */
    @GraphQLDescription("The list of the neighbors")
    fun getNeighbors(): List<NodeSurrogate<T>> = origin.neighbors.map { it.toGraphQLNodeSurrogate() }

    /**
     * @return true if this neighborhood is empty, false otherwise.
     */
    @GraphQLDescription("Whether this neighborhood is empty")
    fun isEmpty(): Boolean = origin.isEmpty

    /**
     * Check whether a node is contained in this neighborhood.
     * @return true if the input node is contained in this neighborhood, false otherwise.
     */
    @GraphQLIgnore
    @GraphQLDescription("Whether the input node is contained in this neighborhood")
    fun contains(node: NodeSurrogate<T>): Boolean = origin.contains(node.origin)
}

/**
 * Converts a [Neighborhood] to a [NeighborhoodSurrogate].
 */
fun <T> Neighborhood<T>.toGraphQLNeighborhoodSurrogate() = NeighborhoodSurrogate(this)
