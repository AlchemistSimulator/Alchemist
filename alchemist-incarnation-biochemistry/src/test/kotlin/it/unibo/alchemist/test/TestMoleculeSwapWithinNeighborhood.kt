/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import it.unibo.alchemist.model.BiochemistryIncarnation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.conditions.AbstractNeighborCondition
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import org.apache.commons.math3.random.MersenneTwister
import kotlin.properties.Delegates

private const val DIRECT_REACTION = "[token] --> [token in neighbor]"
private const val INVERSE_REACTION = "[token in neighbor] --> [token]"
private val INCARNATION = BiochemistryIncarnation()
private val BIOMOLECULE = INCARNATION.createMolecule("token")
private val RANDOM = MersenneTwister()
private val TIME = ExponentialTime<Double>(1.0, RANDOM)
private val LINKING_RULE =
    ConnectWithinDistance<Double, Euclidean2DPosition>(5.0)
private val INITIAL_POSITIONS = Pair(Euclidean2DPosition(0.0, 0.0), Euclidean2DPosition(1.0, 0.0))
private var environment: Environment<Double, Euclidean2DPosition> by Delegates.notNull()
private var nodes: Pair<Node<Double>, Node<Double>> by Delegates.notNull()

class TestMoleculeSwapWithinNeighborhood : StringSpec({
    "send molecule to a neighbor" {
        val reaction = INCARNATION.createReaction(RANDOM, environment, nodes.first, TIME, DIRECT_REACTION)
        reaction shouldHave 2.conditions
        reaction shouldHave 1.neighborConditions
        reaction shouldHave 2.actions
        nodes.first.addReaction(reaction)
        testSimulation()
    }
    "pick molecule from a neighbor" {
        val reaction = INCARNATION.createReaction(RANDOM, environment, nodes.second, TIME, INVERSE_REACTION)
        reaction shouldHave 1.conditions
        reaction shouldHave 1.neighborConditions
        reaction shouldHave 2.actions
        nodes.second.addReaction(reaction)
        testSimulation()
    }
}) {
    override suspend fun beforeTest(testCase: TestCase) {
        environment = BioRect2DEnvironment(INCARNATION)
        nodes = Pair(
            INCARNATION.createNode(RANDOM, environment, null),
            INCARNATION.createNode(RANDOM, environment, null),
        )
        environment.linkingRule = LINKING_RULE
        environment.addNode(nodes.first, INITIAL_POSITIONS.first)
        environment.addNode(nodes.second, INITIAL_POSITIONS.second)
        environment.getNeighborhood(nodes.first).neighbors shouldContain nodes.second
        environment.getNeighborhood(nodes.second).neighbors shouldContain nodes.first
        nodes.first.setConcentration(BIOMOLECULE, 1.0)
    }
}

private fun testSimulation() =
    environment.startSimulationWithoutParameters(
        initialized = {
            nodes.first.getConcentration(BIOMOLECULE) shouldBe 1.0
            nodes.second.getConcentration(BIOMOLECULE) shouldBe 0.0
            nodes.toList().stream().mapToInt { it.reactions.count() }.sum() shouldBe 1
        },
        stepDone = {
            nodes.toList().stream().mapToDouble { it.getConcentration(BIOMOLECULE) }.sum() shouldBe 1.0
        },
        finished = {
            nodes.first.getConcentration(BIOMOLECULE) shouldBe 0.0
            nodes.second.getConcentration(BIOMOLECULE) shouldBe 1.0
        },
    )

private val Int.conditions: Matcher<Reaction<Double>>
    get() = sizeMatcher("conditions") { it.conditions }

private val Int.neighborConditions: Matcher<Reaction<Double>>
    get() = sizeMatcher("neighbor conditions") { it.conditions.filter { c -> c is AbstractNeighborCondition } }

private val Int.actions: Matcher<Reaction<Double>>
    get() = sizeMatcher("actions") { it.actions }

private fun <T> Int.sizeMatcher(collectionName: String, collection: (Reaction<Double>) -> List<T>) =
    object : Matcher<Reaction<Double>> {
        override fun test(value: Reaction<Double>): MatcherResult {
            val actualSize = collection.invoke(value).size
            return MatcherResult(
                actualSize == this@sizeMatcher,
                { "reaction should have ${ this@sizeMatcher } $collectionName but it has $actualSize" },
                { "reaction should not have ${ this@sizeMatcher } $collectionName conditions but it has" },
            )
        }
    }
