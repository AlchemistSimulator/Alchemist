/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.reactions.AbsoluteEvent
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableTimeTest {
    private val environment = Continuous2DEnvironment(BiochemistryIncarnation())

    @Test
    fun `the simulation time notifies every advancement`() {
        listOf(FIRST, SECOND, LAST).forEach { environment.addReaction(AbsoluteEvent(environment, it)) }
        val engine = SteppableEngine(environment)
        val received = mutableListOf<Time>()
        engine.time.onChange { received += it }
        engine.initializeForTest()
        engine.step()
        engine.runPendingCommands()
        engine.step()
        assertEquals(listOf(Time.ZERO, FIRST, SECOND), received)
        assertEquals(SECOND, engine.currentTime)
        assertEquals(SECOND, engine.time.current)
    }

    @Test
    fun `the simulation time has advanced when the event of the step executes`() {
        val engine = SteppableEngine(environment)
        val observedDuringExecution = mutableListOf<Time>()
        val event = AbsoluteEvent(environment, FIRST)
        event.actions = listOf(
            object : AbstractAction<Double>(event) {
                override fun cloneAction(newReaction: Reaction<Double>): Action<Double> =
                    throw UnsupportedOperationException("A test-only action cannot be cloned")

                override fun execute() {
                    observedDuringExecution += engine.time.current
                }
            },
        )
        environment.addReaction(event)
        environment.addReaction(AbsoluteEvent(environment, LAST))
        engine.initializeForTest()
        engine.step()
        assertEquals(listOf(FIRST), observedDuringExecution)
    }

    private companion object {
        private val FIRST: Time = DoubleTime(1.0)
        private val SECOND: Time = DoubleTime(2.0)
        private val LAST: Time = DoubleTime(10.0)
    }
}
