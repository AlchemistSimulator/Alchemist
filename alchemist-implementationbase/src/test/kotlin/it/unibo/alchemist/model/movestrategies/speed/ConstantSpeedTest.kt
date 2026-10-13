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
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConstantSpeedTest {
    @Test
    fun `the movement length fails when the mean delay is not finite and positive`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0).forEach { invalid ->
            assertFailsWith<IllegalStateException> {
                ConstantSpeed<Any, Euclidean2DPosition>(reactionWith { invalid }, SPEED).getNodeMovementLength(null)
            }
        }
    }

    @Test
    fun `the movement length follows the current mean delay of the reaction`() {
        var meanDelay = MEAN_DELAY
        val strategy = ConstantSpeed<Any, Euclidean2DPosition>(reactionWith { meanDelay }, SPEED)
        assertEquals(SPEED * MEAN_DELAY, strategy.getNodeMovementLength(null))
        meanDelay *= 2
        assertEquals(SPEED * meanDelay, strategy.getNodeMovementLength(null))
    }

    private fun reactionWith(meanDelay: () -> Double): NodeReaction<Any> = mockk<TimedNodeReaction> {
        every { this@mockk.meanDelay } answers { meanDelay() }
        every { finitePositiveMeanDelayOrError } answers { callOriginal() }
    }

    private interface TimedNodeReaction :
        NodeReaction<Any>,
        TimeDistributedReaction<Any>

    private companion object {
        private const val MEAN_DELAY = 0.5
        private const val SPEED = 10.0
    }
}
