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
import it.unibo.alchemist.model.NodeReaction

/**
 * A surrogate class for [NodeReaction].
 *
 * @property node the [NodeSurrogate] in which this [ReactionSurrogate] executes
 * @property origin the original Reaction wrapped by this surrogate
 */
@GraphQLDescription("A generic reaction")
data class ReactionSurrogate<T>(
    @param:GraphQLIgnore override val origin: NodeReaction<T>,
    val node: NodeSurrogate<T> = origin.node.toGraphQLNodeSurrogate(),
) : GraphQLSurrogate<NodeReaction<T>>(origin)

/**
 * Converts a [NodeReaction] to a [ReactionSurrogate].
 * @return a [ReactionSurrogate] for this [NodeReaction]
 */
fun <T> NodeReaction<T>.toGraphQLReactionSurrogate() = ReactionSurrogate(this)
