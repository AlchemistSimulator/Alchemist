/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.util

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.PositionSurrogate
import it.unibo.alchemist.model.Position

/**
 * A GraphQL compliant representation of a [Map] storing [it.unibo.alchemist.model.Node]s' ids and
 * their position, represented as a [PositionSurrogate].
 */
@GraphQLDescription("Map of nodes' ids and their position")
data class NodeToPosMap(
    @GraphQLIgnore override val originMap: Map<Int, PositionSurrogate>,
    override val size: Int = originMap.size,
) : GraphQLMap<Int, PositionSurrogate>(originMap, size) {
    /**
     * @return the list of entries in this map.
     */
    @GraphQLDescription("The list of pairs NodeId-Position")
    fun entries(): List<NodeToPosEntry> =
        originMap.map { (id, position) ->
            NodeToPosEntry(id, position)
        }
}

/**
 * A single entry in a [NodeToPosMap], storing a node's id and its position.
 *
 * @param id the node's id
 * @param position the node's position
 */
@GraphQLDescription("The pair NodeId-Position")
data class NodeToPosEntry(
    val id: Int,
    val position: PositionSurrogate,
)

/**
 * Converts a [Map] of [it.unibo.alchemist.model.Node]s' ids and their position to a [NodeToPosMap].
 *
 * @return the [NodeToPosMap] representing the given [Map]
 */
fun <P : Position<out P>> Map<Int, P>.toNodeToPosMap() =
    NodeToPosMap(
        this.mapValues { PositionSurrogateUtils.toPositionSurrogate(it.value) },
    )
