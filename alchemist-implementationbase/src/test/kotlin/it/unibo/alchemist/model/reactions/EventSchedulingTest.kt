/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.ReactionHost
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import org.junit.jupiter.api.Test

class EventSchedulingTest {

    @Test
    fun `a conditional event discards its putative occurrence and redraws when enabled again`() {
        val node = mockk<Node<Any>>(relaxed = true)
        val (environment, setTime) = environmentWithMutableTime()
        val validity = MutableObservable.observe(false, emitOnDistinct = false)
        val condition = ObservableValidityCondition(node, validity)
        val distribution = SequenceDistribution(2.0, 5.0)
        val action = mockk<Action<Any>>(relaxed = true)
        val event = ConditionalEvent(node, distribution).apply {
            conditions = listOf(condition)
            actions = listOf(action)
        }
        every { node.removeReaction(event) } answers { firstArg<Reaction<Any>>().dispose() }

        event.initializationComplete(Time.ZERO, environment)
        assertEquals(Time.INFINITY, event.nextOccurrence.current)
        assertEquals(0, distribution.samples)

        setTime(DoubleTime(10.0))
        validity.current = true
        assertEquals(DoubleTime(12.0), event.nextOccurrence.current)
        assertEquals(1, distribution.samples)

        setTime(DoubleTime(11.0))
        validity.current = true
        assertEquals(DoubleTime(12.0), event.nextOccurrence.current)
        assertEquals(1, distribution.samples)
        validity.current = false
        assertEquals(Time.INFINITY, event.nextOccurrence.current)

        setTime(DoubleTime(15.0))
        validity.current = true
        assertEquals(DoubleTime(20.0), event.nextOccurrence.current)
        assertEquals(2, distribution.samples)

        event.execute()
        verify(exactly = 1) { action.execute() }
        verify(exactly = 1) { node.removeReaction(event) }
        assertEquals(1, condition.readySignals)
        assertEquals(2, distribution.samples)
    }

    @Test
    fun `an absolute event checks conditions only at its occurrence and always unregisters`() {
        listOf(false, true).forEach { validityAtOccurrence ->
            val host = mockk<ReactionHost<Any>>(relaxed = true)
            val node = mockk<Node<Any>>()
            val condition = ObservableValidityCondition(
                node,
                MutableObservable.observe(validityAtOccurrence),
            )
            val action = mockk<Action<Any>>(relaxed = true)
            val event = AbsoluteEvent(host, DoubleTime(10.0)).apply {
                conditions = listOf(condition)
                actions = listOf(action)
            }
            every { host.removeReaction(event) } answers { firstArg<Reaction<Any>>().dispose() }

            event.initializationComplete(Time.ZERO, mockk(relaxed = true))
            assertEquals(DoubleTime(10.0), event.nextOccurrence.current)
            assertEquals(true, event.canExecute().current)

            event.execute()
            verify(exactly = if (validityAtOccurrence) 1 else 0) { action.execute() }
            verify(exactly = 1) { host.removeReaction(event) }
            assertEquals(if (validityAtOccurrence) 1 else 0, condition.readySignals)
        }
    }

    @Test
    fun `cloning a conditional event creates a fresh event and draws once from a fresh distribution`() {
        val sourceDistribution = SequenceDistribution(2.0)
        val source = ConditionalEvent(mockk<Node<Any>>(), sourceDistribution)
        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        val (environment, setTime) = environmentWithMutableTime()
        setTime(DoubleTime(10.0))

        clone.initializationComplete(DoubleTime(10.0), environment)

        val cloneDistribution = assertIs<SequenceDistribution>(clone.timeDistribution)
        assertNotSame(sourceDistribution, cloneDistribution)
        assertEquals(0, sourceDistribution.samples)
        assertEquals(1, cloneDistribution.samples)
        assertEquals(DoubleTime(12.0), clone.nextOccurrence.current)
    }

    private class ObservableValidityCondition<T>(node: Node<T>, validity: MutableObservable<Boolean>) :
        AbstractCondition<T>(node) {
        var readySignals = 0
            private set

        init {
            addObservableDependency(validity)
            setValidity(validity)
        }

        override fun reactionReady() {
            readySignals++
        }
    }

    private class SequenceDistribution(vararg delays: Double) : TimeDistribution<Any> {
        private val configuredDelays = delays.copyOf()
        private val iterator = configuredDelays.iterator()
        var samples = 0
            private set

        override fun sample(): Time = DoubleTime(iterator.nextDouble()).also { samples++ }

        override fun newInstanceOn(node: Node<Any>): TimeDistribution<Any> = SequenceDistribution(*configuredDelays)
    }

    private fun environmentWithMutableTime(): Pair<Environment<Any, Nothing>, (Time) -> Unit> {
        var currentTime: Time = Time.ZERO
        val simulation = mockk<Simulation<Any, Nothing>>()
        every { simulation.time } answers { currentTime }
        val environment = mockk<Environment<Any, Nothing>>()
        every { environment.simulationOrNull } returns simulation
        return environment to { currentTime = it }
    }
}
