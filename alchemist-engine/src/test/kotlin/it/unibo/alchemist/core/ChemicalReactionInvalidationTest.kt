/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import java.util.concurrent.atomic.AtomicInteger
import org.apache.commons.math3.random.MersenneTwister

private val reactionConfigurations = listOf(
    "[a]-->[b]",
    "[a]-->[c]",
    "[b]-->[c]",
    "[c]-->[b]",
)

private val expectedInvalidations = linkedMapOf(
    "[a]-->[b]" to setOf("[a]-->[c]", "[b]-->[c]"),
    "[a]-->[c]" to setOf("[a]-->[b]", "[c]-->[b]"),
    "[b]-->[c]" to setOf("[c]-->[b]"),
    "[c]-->[b]" to setOf("[b]-->[c]"),
)

private data class ChemicalReactionFixture(
    val environment: Environment<Double, Euclidean2DPosition>,
    val reactionsByNode: List<Map<String, NodeReaction<Double>>>,
)

private fun chemicalReactionFixture(): ChemicalReactionFixture {
    val randomGenerator = MersenneTwister(10)
    val incarnation = BiochemistryIncarnation()
    val environment = Continuous2DEnvironment(incarnation).apply {
        linkingRule = ConnectWithinDistance(1.0)
    }
    val reactionsByNode = listOf(0.0 to 0.0, 0.5 to 0.0).map { (x, y) ->
        val node = incarnation.createNode(randomGenerator, environment, null)
        val reactions = reactionConfigurations.associateWith { configuration ->
            incarnation.createReaction(
                randomGenerator,
                environment,
                node,
                ExponentialTime(1.0, randomGenerator),
                configuration,
            ).also(node::addReaction)
        }
        listOf("a", "b", "c").forEach { molecule ->
            node.setConcentration(incarnation.createMolecule(molecule), 1.0)
        }
        environment.addNode(node, environment.makePosition(x, y))
        reactions
    }
    return ChemicalReactionFixture(environment, reactionsByNode)
}

class ChemicalReactionInvalidationTest : StringSpec({
    expectedInvalidations.forEach { (sourceConfiguration, expectedTargetConfigurations) ->
        listOf(0, 1).forEach { sourceNodeIndex ->
            "$sourceConfiguration invalidates only affected reactions from node $sourceNodeIndex" {
                val (environment, reactionsByNode) = chemicalReactionFixture()
                val source = reactionsByNode[sourceNodeIndex].getValue(sourceConfiguration)
                val expectedTargets = expectedTargetConfigurations
                    .map(reactionsByNode[sourceNodeIndex]::getValue)
                    .toSet()
                val allReactions = reactionsByNode.flatMap(Map<String, NodeReaction<Double>>::values)
                val observedReactions = allReactions.filterNot { it === source }
                val emissionCounters = observedReactions.associateWith { AtomicInteger() }
                val subscriptions = mutableListOf<Disposable>()
                try {
                    observedReactions.forEach { target ->
                        target.initializationComplete(Time.ZERO, environment)
                        subscriptions += target.nextOccurrence.subscribe(invokeOnSubscription = false) {
                            emissionCounters.getValue(target).incrementAndGet()
                        }
                    }

                    source.execute()

                    observedReactions.forEach { target ->
                        val emissions = emissionCounters.getValue(target).get()
                        withClue("Unexpected invalidation behavior from $source to $target") {
                            if (target in expectedTargets) {
                                emissions shouldBeGreaterThan 0
                            } else {
                                emissions shouldBe 0
                            }
                        }
                    }
                } finally {
                    subscriptions.forEach(Disposable::dispose)
                    allReactions.forEach(NodeReaction<Double>::dispose)
                }
            }
        }
    }
})
