/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.AbstractReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime

class TestReaction<T>(
    node: Node<T>,
    timeDistribution: TimeDistribution<T>,
) : AbstractReaction<T>(node, timeDistribution) {

    private fun notImplementedError(): Nothing = error("This code should not be reached for this test.")

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): Reaction<T> = notImplementedError()
}

class TestReactionRemoval : FreeSpec({
    "Reactions can be removed from simulation" {
        val incarnation = BiochemistryIncarnation()
        val environment = Continuous2DEnvironment(incarnation)
        val node = GenericNode(environment)
        val customReactions = listOf(TestReaction(node, DiracComb(1.0)))
        customReactions.forEach {
            node.addReaction(it)
        }
        environment.addTerminator { it.simulation.time > DoubleTime(10.0) }
        environment.linkingRule = NoLinks()
        environment.addNode(node, environment.makePosition(0, 0))
        val engine = Engine(environment)
        engine.play()
        engine.schedule {
            environment.removeNode(node)
        }
        engine.run()
        engine.error.isEmpty shouldBe true
    }
})
