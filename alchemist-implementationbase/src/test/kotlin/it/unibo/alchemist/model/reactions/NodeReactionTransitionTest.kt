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
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.math.exp
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.Test

class NodeReactionTransitionTest {

    @Test
    fun `the chemical policy base rejects conditions without explicit rate semantics`() {
        val node = mockk<Node<Any>>()
        val condition = ObservableValidityCondition(node, MutableObservable.observe(true))
        assertFailsWith<IllegalArgumentException> {
            ChemicalNodeReaction(node, ExponentialTime(1.0, mockk(relaxed = true))).conditions = listOf(condition)
        }
    }

    @Test
    fun `invalidation rescales an exponential occurrence without drawing another sample`() {
        val fixture = exponentialFixture(1.0) { 0.5 }
        val (node, environment, propensity, randomGenerator) = fixture
        val samples = { fixture.samples }
        val reaction = ObservableRateReaction(node, ExponentialTime(1.0, randomGenerator), propensity)
        reaction.initializationComplete(Time.ZERO, environment)
        val initialOccurrence = reaction.nextOccurrence.current
        assertEquals(1, samples())
        propensity.current = 2.0
        assertEquals(1, samples())
        assertEquals(initialOccurrence * 0.5, reaction.nextOccurrence.current)
        reaction.updateSchedulingAfterFiring(reaction.nextOccurrence.current)
        assertEquals(2, samples())
    }

    @Test
    fun `invalidation from infinite to finite rate preserves immediate occurrence without drawing`() {
        val node = mockk<Node<Any>>()
        val environment = mockk<Environment<Any, *>>(relaxed = true)
        every { environment.simulationOrNull } returns null
        val propensity = MutableObservable.observe(Double.POSITIVE_INFINITY, emitOnDistinct = false)
        val randomGenerator = mockk<RandomGenerator>()
        var samples = 0
        every { randomGenerator.nextDouble() } answers {
            samples++
            0.5
        }
        val reaction = ObservableRateReaction(
            node,
            ExponentialTime(1.0, DoubleTime(10.0), randomGenerator),
            propensity,
        )

        reaction.initializationComplete(Time.ZERO, environment)
        assertEquals(1, samples)
        assertEquals(DoubleTime(10.0), reaction.nextOccurrence.current)

        propensity.current = 2.0
        assertEquals(1, samples)
        assertEquals(DoubleTime(10.0), reaction.nextOccurrence.current)
        assertEquals(false, reaction.nextOccurrence.current.isInfinite)
    }

    @Test
    fun `invalidation before exponential start time preserves the start time`() {
        val fixture = exponentialFixture(1.0) { 1 - exp(-0.5) }
        val (node, environment, propensity, randomGenerator) = fixture
        val samples = { fixture.samples }
        val reaction = ObservableRateReaction(
            node,
            ExponentialTime(1.0, DoubleTime(10.0), randomGenerator),
            propensity,
        )

        reaction.initializationComplete(Time.ZERO, environment)
        assertEquals(1, samples())
        assertEquals(DoubleTime(10.5), reaction.nextOccurrence.current)

        propensity.current = 2.0
        assertEquals(1, samples())
        assertEquals(DoubleTime(10.25), reaction.nextOccurrence.current)
    }

    @Test
    fun `generic reaction invalidation redraws exponential occurrence`() {
        val node = mockk<Node<Any>>()
        val environment = mockk<Environment<Any, *>>(relaxed = true)
        every { environment.simulationOrNull } returns null
        val validity = MutableObservable.observe(true, emitOnDistinct = false)
        val randomGenerator = mockk<RandomGenerator>()
        var samples = 0
        every { randomGenerator.nextDouble() } answers {
            samples++
            0.5
        }
        val reaction = GenericReaction(node, ExponentialTime(1.0, randomGenerator)).apply {
            conditions = listOf(ObservableValidityCondition(node, validity))
        }
        reaction.initializationComplete(Time.ZERO, environment)
        assertEquals(0, samples)
        validity.current = false
        assertEquals(0, samples)
        assertEquals(Time.INFINITY, reaction.nextOccurrence.current)
        validity.current = true
        assertEquals(1, samples)
    }

    private class ObservableRateCondition<T>(node: Node<T>, rate: MutableObservable<Double>) : AbstractCondition<T>(
        node,
    ) {
        init {
            addObservableDependency(rate)
            setValidity(MutableObservable.observe(true))
        }
    }

    private class ObservableRateReaction<T>(
        node: Node<T>,
        timeDistribution: ExponentialTime<T>,
        private val observableRate: MutableObservable<Double>,
    ) : AbstractMarkovianNodeReaction<T>(node, timeDistribution) {

        init {
            conditions = listOf(ObservableRateCondition(node, observableRate))
        }

        override val rate: Double get() = observableRate.current

        override fun cloneOnNewNode(node: Node<T>, currentTime: Time): ObservableRateReaction<T> =
            makeClone(node, currentTime) { freshGenerator ->
                ObservableRateReaction(node, freshGenerator as ExponentialTime<T>, observableRate)
            }

        override fun onInitializationComplete(atTime: Time, environment: Environment<T, *>) {
            if (!isNewlyInstantiatedProgram) {
                scheduleNextOccurrenceAfterFiring(atTime)
            }
        }
    }

    private class ObservableValidityCondition<T>(node: Node<T>, validity: MutableObservable<Boolean>) :
        AbstractCondition<T>(node) {
        init {
            addObservableDependency(validity)
            setValidity(validity)
        }
    }

    private data class ExponentialFixture(
        val node: Node<Any>,
        val environment: Environment<Any, *>,
        val propensity: MutableObservable<Double>,
        val randomGenerator: RandomGenerator,
        var samples: Int = 0,
    )

    private fun exponentialFixture(propensityValue: Double, sample: () -> Double): ExponentialFixture {
        val node = mockk<Node<Any>>()
        val environment = mockk<Environment<Any, *>>(relaxed = true)
        every { environment.simulationOrNull } returns null
        val propensity = MutableObservable.observe(propensityValue, emitOnDistinct = false)
        val randomGenerator = mockk<RandomGenerator>()
        val fixture = ExponentialFixture(node, environment, propensity, randomGenerator)
        every { randomGenerator.nextDouble() } answers {
            fixture.samples++
            sample()
        }
        return fixture
    }
}
