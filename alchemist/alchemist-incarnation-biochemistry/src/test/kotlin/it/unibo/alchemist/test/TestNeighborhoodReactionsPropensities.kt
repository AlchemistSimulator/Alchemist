/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.TestCase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldHave
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.BiochemistryIncarnation
import it.unibo.alchemist.model.implementations.conditions.AbstractNeighborCondition
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInNeighbor
import it.unibo.alchemist.model.implementations.conditions.JunctionPresentInCell
import it.unibo.alchemist.model.implementations.conditions.NeighborhoodPresent
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.implementations.molecules.Junction
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import it.unibo.alchemist.model.interfaces.CellNode
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble
import kotlin.properties.Delegates

private const val BIOMOLECULE_NEEDED = 5
private const val NEIGHBORHOOD_PRESENT_REACTION = "[5 token] --> [5 token in neighbor]"
private const val JUNCTION_PRESENT_REACTION = "[5 token] + [junction A-B] --> [5 token in neighbor] + [junction A-B]"
private const val BIOMOLECULE_IN_NEIGHBOR_REACTION = "[5 token in neighbor] --> [5 token]"
private val INCARNATION = BiochemistryIncarnation<Euclidean2DPosition>()
private val BIOMOLECULE = INCARNATION.createMolecule("token")
private val BIOMOLECULE_A = INCARNATION.createMolecule("A")
private val BIOMOLECULE_B = INCARNATION.createMolecule("B")
private val JUNCTION = Junction("A-B", mapOf(Pair(BIOMOLECULE_A, 1.0)), mapOf(Pair(BIOMOLECULE_B, 1.0)))
private val RANDOM = MersenneTwister()
private val TIME = ExponentialTime<Double>(1.0, RANDOM)
private val LINKING_RULE = ConnectWithinDistance<Double, Euclidean2DPosition>(5.0)
private val POSITION = Euclidean2DPosition(0.0, 0.0)
private var environment: Environment<Double, Euclidean2DPosition> by Delegates.notNull()
private var centralNode: CellNode<Euclidean2DPosition> by Delegates.notNull()
private var neighbors: List<CellNode<Euclidean2DPosition>> by Delegates.notNull()

class TestNeighborhoodReactionsPropensities : StringSpec({
    "test neighborhood present propensities" {
        testSimulation(NEIGHBORHOOD_PRESENT_REACTION)
    }
    "test junction present propensities" {
        0.rangeTo(9).forEach {
            0.rangeTo(it).forEach { _ -> centralNode.addJunction(JUNCTION, neighbors[it]) }
        }
        testSimulation(JUNCTION_PRESENT_REACTION)
    }
    "test biomolecule in neighborhood propensities" {
        testSimulation(BIOMOLECULE_IN_NEIGHBOR_REACTION)
    }
}) {
    override fun beforeTest(testCase: TestCase) {
        environment = BioRect2DEnvironment()
        environment.linkingRule = LINKING_RULE
        centralNode = CellNodeImpl(environment)
        centralNode.setConcentration(BIOMOLECULE, 100.0)
        environment.addNode(centralNode, POSITION)
        neighbors = 1.rangeTo(10)
                .map { Pair(it * 10.0, CellNodeImpl(environment)) }
                .onEach { it.second.setConcentration(BIOMOLECULE, it.first) }
                .map { it.second }
                .onEach { environment.addNode(it, POSITION) }
        environment.getNeighborhood(centralNode).neighbors shouldContainExactly neighbors
    }
}

private fun testSimulation(reactionText: String) {
    val reaction = INCARNATION.createReaction(RANDOM, environment, centralNode, TIME, reactionText)
    centralNode.addReaction(reaction)
    environment.startSimulationWithoutParameters(
            stepDone = {
                val checks = reaction.conditions
                        .filterIsInstance<AbstractNeighborCondition<Double>>()
                        .flatMap { it.validNeighbors.map { (node, value) -> Container(it, node, value) } }
                for (it in checks) {
                    it shouldHave it.expectedPropensity
                }
            }
    )
}

private data class Container(
    val condition: AbstractNeighborCondition<Double>,
    val node: Node<Double>,
    val propensity: Double
)

private val Container.expectedPropensity: Matcher<Container>
    get() = object : Matcher<Container> {
        @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
        override fun test(value: Container): Result {
            val expectedPropensity = when (condition) {
                is NeighborhoodPresent -> node.neighborhoodPresentPropensity
                is JunctionPresentInCell -> node.junctionPresentPropensity
                is BiomolPresentInNeighbor -> node.biomoleculeInNeighborPropensity
                else -> throw IllegalStateException("Unknown neighbor condition")
            }
            return Result(
                expectedPropensity == propensity,
                "node $node should have propensity $expectedPropensity for condition $condition but it has $propensity",
                "node $node should not have propensity $expectedPropensity for condition $condition but it has"
            )
        }
    }

private val Node<Double>.neighborhoodPresentPropensity: Double
    get() = checkCellNodeAndGetPropensity { 1.0 }

private val Node<Double>.junctionPresentPropensity: Double
    get() = checkCellNodeAndGetPropensity {
        centralNode.junctions.getOrDefault(JUNCTION, emptyMap()).getOrDefault(it, 0).toDouble()
    }

private val Node<Double>.biomoleculeInNeighborPropensity: Double
    get() = checkCellNodeAndGetPropensity {
        binomialCoefficientDouble(it.getConcentration(BIOMOLECULE).toInt(), BIOMOLECULE_NEEDED)
    }

private fun Node<Double>.checkCellNodeAndGetPropensity(propensityFunction: (CellNode<*>) -> Double) =
    if (this is CellNode<*>) { propensityFunction(this) } else { 0.0 }