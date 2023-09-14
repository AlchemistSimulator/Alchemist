/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.ReactionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLNodeSurrogate
import it.unibo.alchemist.model.Reaction

fun <T>checkReactionSurrogate(reaction: Reaction<T>, reactionSurrogate: ReactionSurrogate<T>) {
    reaction.inputContext shouldBe reactionSurrogate.inputContext
    reaction.outputContext shouldBe reactionSurrogate.outputContext
    reaction.node.toGraphQLNodeSurrogate() shouldBe reactionSurrogate.node
}
