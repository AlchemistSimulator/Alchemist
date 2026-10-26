/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TerminatedSimulationMutationTest {
    private val environment = Continuous2DEnvironment(BiochemistryIncarnation())
    private val node = GenericNode(environment).also {
        it.addReaction(GenericReaction(it, DiracComb(1.0)))
        environment.addNode(it, environment.makePosition(0, 0))
    }
    private val engine = Engine(environment).apply {
        environment.addTerminator { it.simulation.currentTime >= END }
    }

    @Test
    fun `a node can be fully removed once the simulation is terminated`() {
        terminate()
        environment.removeNode(node)
        assertFalse(node in environment)
        assertEquals(0, environment.nodeCount.current)
        assertTrue(engine.error.isNone())
    }

    @Test
    fun `reactions can be added and removed once the simulation is terminated`() {
        terminate()
        val reaction = GenericReaction(node, DiracComb(1.0))
        node.addReaction(reaction)
        assertTrue(reaction in node.reactions.current)
        node.removeReaction(reaction)
        assertFalse(reaction in node.reactions.current)
    }

    @Test
    fun `the model can be changed when the simulation finishes`() {
        engine.addOutputMonitor(
            object : OutputMonitor<Double, Euclidean2DPosition> {
                override fun finished(environment: Environment<Double, Euclidean2DPosition>, time: Time, step: Long) {
                    environment.removeNode(environment.nodes.current.single())
                }
            },
        )
        terminate()
        assertTrue(engine.error.isNone(), engine.error.fold({ "" }) { it.stackTraceToString() })
        assertEquals(0, environment.nodeCount.current)
    }

    private fun terminate() {
        engine.play()
        engine.run()
        assertEquals(SimulationState.TERMINATED, engine.state)
    }

    private companion object {
        private val END: Time = DoubleTime(3.0)
    }
}
