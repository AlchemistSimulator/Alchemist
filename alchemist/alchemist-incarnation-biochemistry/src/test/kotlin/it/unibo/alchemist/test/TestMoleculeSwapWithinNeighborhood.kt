/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import io.kotlintest.*
import io.kotlintest.inspectors.forAny
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.specs.AbstractAnnotationSpec
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.model.BiochemistryIncarnation
import it.unibo.alchemist.model.implementations.conditions.AbstractNeighborCondition
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInNeighbor
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.*
import org.apache.commons.math3.random.MersenneTwister
import java.util.stream.Collectors
import java.util.stream.Stream
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

class TestNeighborhood : StringSpec() {
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

    init {
        "send molecule to a neighbor" {
            val reaction = INCARNATION.createReaction(RANDOM, environment, nodes.first, TIME, DIRECT_REACTION)
            reaction shouldHave 2.conditions
            reaction shouldHave 1.neighborConditions
            reaction shouldHave 2.actions
            nodes.first.addReaction(reaction)
            startSimulation()
        }
        "pick molecule from a neighbor" {
            val reaction = INCARNATION.createReaction(RANDOM, environment, nodes.second, TIME, INVERSE_REACTION)
            reaction shouldHave 1.conditions
            reaction shouldHave 1.neighborConditions
            reaction shouldHave 2.actions
            nodes.second.addReaction(reaction)
            startSimulation()
        }
    }
}

private fun startSimulation() {
    val simulation = Engine(environment, DoubleTime.INFINITE_TIME)
    simulation.addOutputMonitor(object: OutputMonitor<Double, Euclidean2DPosition> {
        override fun initialized(e: Environment<Double, Euclidean2DPosition>) {
            nodes.first.getConcentration(BIOMOLECULE) shouldBe 1.0
            nodes.second.getConcentration(BIOMOLECULE) shouldBe 0.0
            nodes.toList().stream().mapToInt { it.reactions.count() }.sum() shouldBe 1
        }

        override fun stepDone(e: Environment<Double, Euclidean2DPosition>, r: Reaction<Double>, t: Time, s: Long) {
            nodes.toList().stream().mapToDouble { it.getConcentration(BIOMOLECULE) }.sum() shouldBe 1.0
        }

        override fun finished(e: Environment<Double, Euclidean2DPosition>, t: Time, s: Long) {
            nodes.first.getConcentration(BIOMOLECULE) shouldBe 0.0
            nodes.second.getConcentration(BIOMOLECULE) shouldBe 1.0
        }
    })
    simulation.play()
    simulation.run()
}

private fun matcher(test: (Reaction<Double>) -> Result) = object: Matcher<Reaction<Double>> {
    override fun test(reaction: Reaction<Double>) = test.invoke(reaction)
}

private val Int.conditions: Matcher<Reaction<Double>>
    get() = matcher{ Result(
            it.conditions.size == this@conditions,
            "reaction should have ${this@conditions} conditions but it has ${it.conditions.size}",
            "reaction should not have ${this@conditions} conditions but it has"
    )}

private val Int.neighborConditions: Matcher<Reaction<Double>>
    get() = matcher{ Result(
            it.conditions.filter{ c -> c is AbstractNeighborCondition }.size == this@neighborConditions,
            "reaction should have ${this@neighborConditions} neighbor conditions but it has " +
                    "${it.conditions.filter{ c -> c is AbstractNeighborCondition }.size}",
            "reaction should not have ${this@neighborConditions} conditions but it has"
    )}


private val Int.actions: Matcher<Reaction<Double>>
    get() = matcher{
        Result(
            it.actions.size == this@actions,
            "reaction should have ${this@actions} actions but it has",
            "reaction should not have ${this@actions} actions but it has"
    )}