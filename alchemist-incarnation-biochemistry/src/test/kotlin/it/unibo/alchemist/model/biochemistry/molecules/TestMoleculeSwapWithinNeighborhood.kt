/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.molecules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.conditions.AbstractNeighborCondition
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.biochemistry.startSimulationWithoutParameters
import it.unibo.alchemist.model.biochemistry.util.Nodes.getOrZeroConcentration
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.commons.math3.random.MersenneTwister
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val DIRECT_REACTION = "[token] --> [token in neighbor]"
private const val INVERSE_REACTION = "[token in neighbor] --> [token]"
private val INCARNATION = BiochemistryIncarnation()
private val BIOMOLECULE = INCARNATION.createMolecule("token")
private val RANDOM = MersenneTwister()
private val TIME = ExponentialTime<Double>(1.0, RANDOM)
private val INITIAL_POSITIONS = Pair(Euclidean2DPosition(0.0, 0.0), Euclidean2DPosition(1.0, 0.0))
private var environment: Environment<Double, Euclidean2DPosition> by Delegates.notNull()
private var nodesToAdd: Pair<Node<Double>, Node<Double>> by Delegates.notNull()

class TestMoleculeSwapWithinNeighborhood {
    @BeforeEach
    fun setUp() {
        environment = BioRect2DEnvironment(INCARNATION)
        nodesToAdd = Pair(
            INCARNATION.createNode(RANDOM, environment, null),
            INCARNATION.createNode(RANDOM, environment, null),
        )
        with(environment) {
            networkModel = connectWithinDistance(5.0)
            addNode(nodesToAdd.first, INITIAL_POSITIONS.first)
            addNode(nodesToAdd.second, INITIAL_POSITIONS.second)
            assertTrue(neighborhoodOf(nodesToAdd.first).current.neighbors.contains(nodesToAdd.second))
            assertTrue(neighborhoodOf(nodesToAdd.second).current.neighbors.contains(nodesToAdd.first))
        }
        nodesToAdd.first[BIOMOLECULE] = 1.0
    }

    @Test
    fun `it should be possible to send molecules to a neighbor`() {
        val reaction = INCARNATION.createReaction(RANDOM, environment, nodesToAdd.first, TIME, DIRECT_REACTION)
        assertEquals(2, reaction.conditions.size)
        assertEquals(1, reaction.neighborConditions.size)
        assertEquals(2, reaction.actions.size)
        nodesToAdd.first.addReaction(reaction)
        testSimulation()
    }

    @Test
    fun `it should be possible to pick molecules from a neighbor`() {
        val reaction = INCARNATION.createReaction(RANDOM, environment, nodesToAdd.second, TIME, INVERSE_REACTION)
        assertEquals(1, reaction.conditions.size)
        assertEquals(1, reaction.neighborConditions.size)
        assertEquals(2, reaction.actions.size)
        nodesToAdd.second.addReaction(reaction)
        testSimulation()
    }
}

private fun testSimulation() = environment.startSimulationWithoutParameters(
    initialized = {
        assertEquals(1.0, nodesToAdd.first.getOrZeroConcentration(BIOMOLECULE))
        assertEquals(0.0, nodesToAdd.second.getOrZeroConcentration(BIOMOLECULE))
        assertEquals(1, nodesToAdd.toList().sumOf { it.reactions.current.count() })
    },
    stepDone = {
        assertEquals(1.0, nodesToAdd.toList().sumOf { it.getOrZeroConcentration(BIOMOLECULE) })
    },
    finished = {
        assertEquals(0.0, nodesToAdd.first.getOrZeroConcentration(BIOMOLECULE))
        assertEquals(1.0, nodesToAdd.second.getOrZeroConcentration(BIOMOLECULE))
    },
)

private val NodeReaction<Double>.neighborConditions: List<AbstractNeighborCondition<*>>
    get() = conditions.filterIsInstance<AbstractNeighborCondition<*>>()
