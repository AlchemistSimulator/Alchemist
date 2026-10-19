/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.AbsoluteEvent
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AbsoluteEventRegistrationTest {
    private val environment = Continuous2DEnvironment(BiochemistryIncarnation())
    private val engine = SteppableEngine(environment)
    private val node = GenericNode(environment).also { environment.addNode(it, environment.makePosition(0, 0)) }

    @Test
    fun `an absolute event at the current time is registered and fires`() {
        advanceTo(NOW)
        val event = AbsoluteEvent(node, NOW)
        node.addReaction(event)
        engine.runPendingCommands()
        engine.step()
        assertFalse(event in node.reactions.current)
        assertEquals(NOW, engine.time)
    }

    @Test
    fun `an absolute event in the past fails at registration, naming the event and the current time`() {
        advanceTo(NOW)
        val event = AbsoluteEvent(node, PAST)
        node.addReaction(event)
        val error = assertFailsWith<IllegalArgumentException> { engine.runPendingCommands() }
        assertTrue(error.message.orEmpty().contains(event.toString()))
        assertTrue(error.message.orEmpty().contains(NOW.toString()))
    }

    private fun advanceTo(time: Time) {
        environment.addReaction(AbsoluteEvent(environment, time))
        // A later event keeps the simulation alive after the first one fires.
        environment.addReaction(AbsoluteEvent(environment, LATER))
        engine.initializeForTest()
        engine.step()
        assertEquals(time, engine.time)
    }

    private class SteppableEngine(environment: Continuous2DEnvironment<Double>) :
        Engine<Double, Euclidean2DPosition>(environment) {
        fun initializeForTest() = initialize()

        fun runPendingCommands() {
            while (commands.isNotEmpty()) {
                processCommand(commands.poll())
            }
        }

        fun step() = doStep()
    }

    private companion object {
        private val LATER: Time = DoubleTime(20.0)
        private val NOW: Time = DoubleTime(10.0)
        private val PAST: Time = DoubleTime(5.0)
    }
}
