/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies.speed

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.DiracComb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GloballyConstantSpeedTest {
    @Test
    fun `construction fails when the mean delay is not finite and positive`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0).forEach { invalid ->
            assertFailsWith<IllegalStateException> {
                GloballyConstantSpeed<Any, Euclidean2DPosition>(reactionWith(distributionWith { invalid }), SPEED)
            }
        }
    }

    @Test
    fun `the movement length fails when the mean delay is no longer finite and positive`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0).forEach { invalid ->
            var meanDelay = 1 / DISTRIBUTION_RATE
            val strategy = GloballyConstantSpeed<Any, Euclidean2DPosition>(
                reactionWith(distributionWith { meanDelay }),
                SPEED,
            )
            meanDelay = invalid
            assertFailsWith<IllegalStateException> { strategy.getNodeMovementLength(null) }
        }
    }

    @Test
    fun `the movement length follows the mean delay of the time distribution rather than of the reaction`() {
        val strategy = GloballyConstantSpeed<Any, Euclidean2DPosition>(
            reactionWith(DiracComb(DISTRIBUTION_RATE)),
            SPEED,
        )
        assertEquals(SPEED / DISTRIBUTION_RATE, strategy.getNodeMovementLength(null))
    }

    private fun distributionWith(meanDelay: () -> Double) = mockk<TimeDistribution<Any>> {
        every { this@mockk.meanDelay } answers { meanDelay() }
        every { finitePositiveMeanDelayOrError } answers { callOriginal() }
    }

    private fun reactionWith(distribution: TimeDistribution<Any>) = mockk<TimedNodeReaction> {
        // A chemical reaction, for instance, scales the distribution rate by the propensity of its conditions.
        every { meanDelay } returns 1 / (DISTRIBUTION_RATE * PROPENSITY)
        every { timeDistribution } returns distribution
    }

    private interface TimedNodeReaction :
        NodeReaction<Any>,
        TimeDistributedReaction<Any>

    private companion object {
        private const val DISTRIBUTION_RATE = 2.0
        private const val PROPENSITY = 3.0
        private const val SPEED = 10.0
    }
}
