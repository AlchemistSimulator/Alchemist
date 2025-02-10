/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.reactions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.conditions.AbstractNeighborCondition
import it.unibo.alchemist.model.biochemistry.conditions.BiomolPresentInNeighbor
import it.unibo.alchemist.model.biochemistry.conditions.JunctionPresentInCell
import it.unibo.alchemist.model.biochemistry.conditions.NeighborhoodPresent
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import it.unibo.alchemist.model.biochemistry.startSimulationWithoutParameters
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates
import kotlin.test.assertEquals

private const val BIOMOLECULE_NEEDED = 5
private const val NEIGHBORHOOD_PRESENT_REACTION = "[5 token] --> [5 token in neighbor]"
private const val JUNCTION_PRESENT_REACTION = "[5 token] + [junction A-B] --> [5 token in neighbor] + [junction A-B]"
private const val BIOMOLECULE_IN_NEIGHBOR_REACTION = "[5 token in neighbor] --> [5 token]"
private val INCARNATION = BiochemistryIncarnation()
private val BIOMOLECULE = INCARNATION.createMolecule("token")
private val BIOMOLECULE_A = INCARNATION.createMolecule("A")
private val BIOMOLECULE_B = INCARNATION.createMolecule("B")
private val JUNCTION =
    Junction(
        "A-B",
        mapOf(Pair(BIOMOLECULE_A, 1.0)),
        mapOf(Pair(BIOMOLECULE_B, 1.0)),
    )
private val RANDOM = MersenneTwister()
private val TIME = ExponentialTime<Double>(1.0, RANDOM)
private val LINKING_RULE = ConnectWithinDistance<Double, Euclidean2DPosition>(5.0)
private val POSITION = Euclidean2DPosition(0.0, 0.0)
private var environment: Environment<Double, Euclidean2DPosition> by Delegates.notNull()
private var centralNode: Node<Double> by Delegates.notNull()
private var neighbors: List<Node<Double>> by Delegates.notNull()

class TestNeighborhoodReactionsPropensities {
    @BeforeEach
    fun setUp() {
        environment = BioRect2DEnvironment(INCARNATION)
        environment.linkingRule = LINKING_RULE
        centralNode = INCARNATION.createNode(RANDOM, environment, null)
        centralNode.setConcentration(BIOMOLECULE, 100.0)
        environment.addNode(centralNode, POSITION)
        neighbors =
            (1..10)
                .map { Pair(it * 10.0, INCARNATION.createNode(RANDOM, environment, null)) }
                .onEach { it.second.setConcentration(BIOMOLECULE, it.first) }
                .map { it.second }
                .onEach { environment.addNode(it, POSITION) }
        assertEquals(neighbors.toList(), environment.getNeighborhood(centralNode).neighbors.toList())
    }

    @Test
    fun `the propensity of a reaction sending molecules to a neighbor should be neighbor-sensitive`() {
        testSimulation(NEIGHBORHOOD_PRESENT_REACTION)
    }

    @Test
    fun `the propensity of a reaction requiring a junction should consider the junctions count`() {
        for (i in 0..9) {
            repeat(i + 1) {
                centralNode
                    .asProperty<Double, CellProperty<Euclidean2DPosition>>()
                    .addJunction(JUNCTION, neighbors[i])
            }
        }
        testSimulation(JUNCTION_PRESENT_REACTION)
    }

    @Test
    fun `the propensity of a reaction reading molecules from neighbors should be neighbor-sensitive`() {
        testSimulation(BIOMOLECULE_IN_NEIGHBOR_REACTION)
    }
}

private fun testSimulation(reactionText: String) {
    val reaction = INCARNATION.createReaction(RANDOM, environment, centralNode, TIME, reactionText)
    centralNode.addReaction(reaction)
    environment.startSimulationWithoutParameters(
        stepDone = {
            val checks =
                reaction.conditions
                    .filterIsInstance<AbstractNeighborCondition<Double>>()
                    .flatMap { it.validNeighbors.map { (node, value) -> Container(it, node, value) } }
            for (it in checks) {
                assertEquals(it.expectedPropensity, it.propensity)
            }
        },
    )
}

private data class Container(
    val condition: AbstractNeighborCondition<Double>,
    val node: Node<Double>,
    val propensity: Double,
)

private val Container.expectedPropensity: Double
    get() =
        when (condition) {
            is NeighborhoodPresent -> node.neighborhoodPresentPropensity
            is JunctionPresentInCell -> node.junctionPresentPropensity
            is BiomolPresentInNeighbor -> node.biomoleculeInNeighborPropensity
            else -> error("Unknown neighbor condition")
        }

private val Node<Double>.neighborhoodPresentPropensity: Double
    get() = checkCellNodeAndGetPropensity { 1.0 }

private val Node<Double>.junctionPresentPropensity: Double
    get() =
        checkCellNodeAndGetPropensity {
            centralNode
                .asProperty<Double, CellProperty<Euclidean2DPosition>>()
                .junctions
                .getOrDefault(JUNCTION, emptyMap())
                .getOrDefault(it, 0)
                .toDouble()
        }

private val Node<Double>.biomoleculeInNeighborPropensity: Double
    get() =
        checkCellNodeAndGetPropensity {
            binomialCoefficientDouble(it.getConcentration(BIOMOLECULE).toInt(), BIOMOLECULE_NEEDED)
        }

private fun Node<Double>.checkCellNodeAndGetPropensity(propensityFunction: (Node<Double>) -> Double) =
    if (this.asPropertyOrNull(CellProperty::class) != null) {
        propensityFunction(this)
    } else {
        0.0
    }
