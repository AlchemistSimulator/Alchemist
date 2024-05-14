/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.AbstractReaction
import it.unibo.alchemist.model.reactions.ChemicalReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.test.AlchemistTesting.createEmptyEnvironment
import org.junit.jupiter.api.Assertions.fail

class CustomReaction<T>(
    customReactionNode: Node<T>,
    customReactionTimeDistribution: TimeDistribution<T>,
) : AbstractReaction<T>(customReactionNode, customReactionTimeDistribution) {

    init {
        setInputContext(Context.GLOBAL)
        setOutputContext(Context.GLOBAL)
    }

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): Reaction<T> =
        makeClone { ChemicalReaction(node, timeDistribution.cloneOnNewNode(node, currentTime)) }

    override fun updateInternalStatus(currentTime: Time?, hasBeenExecuted: Boolean, environment: Environment<T, *>?) { }
}

class TestReactionRemoval : FreeSpec({

    "A Reaction with input and output context set as GLOBAL can be deleted from the incarnation" {

        val environment = createEmptyEnvironment<Any>()
        val node = GenericNode(environment)
        val timeDistribution = DiracComb<Any>(5.0)
        node.addReaction(CustomReaction(node, timeDistribution))

        environment.simulation.schedule {
            val customReaction = CustomReaction(node, timeDistribution)
            node.addReaction(customReaction)
            environment.simulation.reactionAdded(customReaction)
            customReaction.inputContext shouldBe Context.GLOBAL
            customReaction.outputContext shouldBe Context.GLOBAL
            environment.nodes.count() shouldBe 1
            environment.nodes.first().reactions shouldBe customReaction
            try {
                node.removeReaction(customReaction)
                environment.simulation.reactionRemoved(customReaction)
                node.reactions.isEmpty() shouldBe true
            } catch (e: Exception) {
                fail(e)
            }
        }
    }
})
