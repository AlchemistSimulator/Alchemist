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
import it.unibo.alchemist.model.Context.GLOBAL
import it.unibo.alchemist.model.Context.LOCAL
import it.unibo.alchemist.model.Environment
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

class GlobalContextsReaction<T>(
    node: Node<T>,
    timeDistribution: TimeDistribution<T>,
    inGlobal: Boolean,
    outGlobal: Boolean,
) : AbstractReaction<T>(node, timeDistribution) {

    init {
        setInputContext(if (inGlobal) GLOBAL else LOCAL)
        setOutputContext(if (outGlobal) GLOBAL else LOCAL)
    }

    private fun notImplementedError(): Nothing = error("This code should not be reached for this test.")

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): Reaction<T> = notImplementedError()

    override fun updateInternalStatus(currentTime: Time?, hasBeenExecuted: Boolean, environment: Environment<T, *>?) =
        notImplementedError()
}

class TestReactionRemoval : FreeSpec({

    "All possible combination of GLOBAL/LOCAL reactions as in/out context can be removed from simulation" {
        val incarnation = BiochemistryIncarnation()
        val environment = Continuous2DEnvironment(incarnation)
        val node = GenericNode(environment)
        val bools = listOf(true, false)
        val customReactions = bools
            .flatMap { first -> bools.map { first to it } }
            .map { (input, out) -> GlobalContextsReaction(node, DiracComb(1.0), input, out) }
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
