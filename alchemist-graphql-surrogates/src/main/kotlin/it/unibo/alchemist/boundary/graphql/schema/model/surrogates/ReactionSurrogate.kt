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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Reaction

/**
 * A surrogate class for a node-hosted [Reaction].
 *
 * @property node the [NodeSurrogate] in which this [ReactionSurrogate] executes
 * @property origin the original Reaction wrapped by this surrogate
 */
@GraphQLDescription("A generic reaction")
data class ReactionSurrogate<T>(@param:GraphQLIgnore override val origin: Reaction<T>, val node: NodeSurrogate<T>) :
    GraphQLSurrogate<Reaction<T>>(origin)

/**
 * Converts a node-hosted [Reaction] to a [ReactionSurrogate].
 * @param host the node whose reaction collection owns this reaction
 * @return a [ReactionSurrogate] for this [Reaction]
 */
fun <T> Reaction<T>.toGraphQLReactionSurrogate(host: Node<T>) = ReactionSurrogate(this, host.toGraphQLNodeSurrogate())

/**
 * Converts a [NodeReaction] to a [ReactionSurrogate].
 * @return a [ReactionSurrogate] for this [NodeReaction]
 */
fun <T> NodeReaction<T>.toGraphQLReactionSurrogate() = toGraphQLReactionSurrogate(node)
