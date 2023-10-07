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
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Reaction

/**
 * A surrogate class for [Reaction].
 * @param inputContext the input context of the reaction
 * @param outputContext the output context of the reaction
 * @param node the [NodeSurrogate] in which this [ReactionSurrogate] executes
 */
@GraphQLDescription("A generic reaction")
data class ReactionSurrogate<T>(
    @GraphQLIgnore override val origin: Reaction<T>,
    val inputContext: Context = origin.inputContext,
    val outputContext: Context = origin.outputContext,
    val node: NodeSurrogate<T> = origin.node.toGraphQLNodeSurrogate(),
) : GraphQLSurrogate<Reaction<T>>(origin)

/**
 * Converts a [Reaction] to a [ReactionSurrogate].
 * @return a [ReactionSurrogate] for this [Reaction]
 */
fun <T> Reaction<T>.toGraphQLReactionSurrogate() = ReactionSurrogate(this)
