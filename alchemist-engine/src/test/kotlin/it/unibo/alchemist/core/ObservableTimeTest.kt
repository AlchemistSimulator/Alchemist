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
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.AbsoluteEvent
import it.unibo.alchemist.model.reactions.AbstractNodeReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `an event whose conditions no longer hold at the new time is skipped, and the step still counts`() {
        val engine = SteppableEngine(environment)
        val node = GenericNode(environment).also { environment.addNode(it, environment.makePosition(0, 0)) }
        var executions = 0
        val reaction = countingReaction(node) { executions++ }
        reaction.conditions = listOf(ValidBefore(reaction, engine.time, FIRST))
        node.addReaction(reaction)
        val firedPerStep = engine.recordFiredReactions()
        engine.initializeForTest()
        engine.step()
        assertEquals(0, executions)
        assertEquals(listOf<Reaction<Double>?>(null), firedPerStep)
        assertEquals(1, engine.step)
        assertTrue(engine.currentTime >= FIRST)
        assertEquals(Time.INFINITY, reaction.nextOccurrence.current)
    }

    @Test
    fun `an event removed by a time observer is skipped, and the step still counts`() {
        val event = AbsoluteEvent(environment, FIRST)
        environment.addReaction(event)
        environment.addReaction(AbsoluteEvent(environment, LAST))
        val engine = SteppableEngine(environment)
        val firedPerStep = engine.recordFiredReactions()
        engine.time.onChange(notifyOnRegistration = false) { environment.removeReaction(event) }
        engine.initializeForTest()
        engine.step()
        assertEquals(listOf<Reaction<Double>?>(null), firedPerStep)
        assertEquals(1, engine.step)
        assertEquals(FIRST, engine.currentTime)
    }

    @Test
    fun `an event whose node is removed by a time observer is skipped`() {
        val engine = SteppableEngine(environment)
        val node = GenericNode(environment).also { environment.addNode(it, environment.makePosition(0, 0)) }
        var executions = 0
        node.addReaction(countingReaction(node) { executions++ })
        environment.addReaction(AbsoluteEvent(environment, LAST))
        val firedPerStep = engine.recordFiredReactions()
        engine.time.onChange(notifyOnRegistration = false) { environment.removeNode(node) }
        engine.initializeForTest()
        engine.step()
        assertEquals(0, executions)
        assertEquals(listOf<Reaction<Double>?>(null), firedPerStep)
    }

    // A reaction occurring once per time unit from FIRST, running mutation when it fires.
    private fun countingReaction(node: Node<Double>, mutation: () -> Unit) =
        object : AbstractNodeReaction<Double>(node, DiracComb(FIRST, 1.0)) {
            override fun cloneOnNewNode(node: Node<Double>): NodeReaction<Double> =
                throw UnsupportedOperationException("A test-only reaction cannot be cloned")

            override fun performModelMutation() = mutation()
        }

    // The reaction reported by each step to the monitors, null for the steps firing none.
    private fun SteppableEngine.recordFiredReactions(): List<Reaction<Double>?> =
        mutableListOf<Reaction<Double>?>().also { fired ->
            addOutputMonitor(
                object : OutputMonitor<Double, Euclidean2DPosition> {
                    override fun stepDone(
                        environment: Environment<Double, Euclidean2DPosition>,
                        reaction: Reaction<Double>?,
                        time: Time,
                        step: Long,
                    ) {
                        fired += reaction
                    }
                },
            )
        }

    // A condition holding while the observed time is before a threshold.
    private class ValidBefore(reaction: Reaction<Double>, time: Observable<Time>, threshold: Time) :
        AbstractCondition<Double>(reaction) {
        init {
            setValidity(time.map { it < threshold })
        }

        override fun cloneCondition(newReaction: Reaction<Double>): Condition<Double> =
            throw UnsupportedOperationException("A test-only condition cannot be cloned")
    }

    private companion object {
        private val FIRST: Time = DoubleTime(1.0)
        private val SECOND: Time = DoubleTime(2.0)
        private val LAST: Time = DoubleTime(10.0)
    }
}
