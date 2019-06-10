/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.TestCase
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldHave
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.BiochemistryIncarnation
import it.unibo.alchemist.model.implementations.conditions.AbstractNeighborCondition
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import it.unibo.alchemist.model.interfaces.CellNode
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.random.MersenneTwister
import kotlin.properties.Delegates

private const val DIRECT_REACTION = "[token] --> [token in neighbor]"
private const val INVERSE_REACTION = "[token in neighbor] --> [token]"
private val INCARNATION = BiochemistryIncarnation<Euclidean2DPosition>()
private val BIOMOLECULE = INCARNATION.createMolecule("token")
private val RANDOM = MersenneTwister()
private val TIME = ExponentialTime<Double>(1.0, RANDOM)
private val LINKING_RULE = ConnectWithinDistance<Double, Euclidean2DPosition>(5.0)
private val INITIAL_POSITIONS = Pair(Euclidean2DPosition(0.0, 0.0), Euclidean2DPosition(1.0, 0.0))
private var environment: Environment<Double, Euclidean2DPosition> by Delegates.notNull()
private var nodes: Pair<CellNode<Euclidean2DPosition>, CellNode<Euclidean2DPosition>> by Delegates.notNull()

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
    override fun beforeTest(testCase: TestCase) {
        environment = BioRect2DEnvironment()
        nodes = Pair(CellNodeImpl(environment), CellNodeImpl(environment))
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
            }
    )

private val Int.conditions: Matcher<Reaction<Double>>
    get() = sizeMatcher("conditions") { it.conditions }

private val Int.neighborConditions: Matcher<Reaction<Double>>
    get() = sizeMatcher("neighbor conditions") { it.conditions.filter { c -> c is AbstractNeighborCondition } }

private val Int.actions: Matcher<Reaction<Double>>
    get() = sizeMatcher("actions") { it.actions }

private fun <T> Int.sizeMatcher(collectionName: String, collection: (Reaction<Double>) -> List<T>) =
    object : Matcher<Reaction<Double>> {
        override fun test(value: Reaction<Double>): Result {
            val actualSize = collection.invoke(value).size
            return Result(
                actualSize == this@sizeMatcher,
                "reaction should have ${ this@sizeMatcher } $collectionName but it has $actualSize",
                "reaction should not have ${ this@sizeMatcher } $collectionName conditions but it has"
            )
        }
    }