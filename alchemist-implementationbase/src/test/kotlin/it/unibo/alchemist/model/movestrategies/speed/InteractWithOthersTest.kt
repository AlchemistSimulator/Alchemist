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
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InteractWithOthersTest {
    private val environment: Environment<Any, Euclidean2DPosition> = mockk {
        every { nodesAround(any<Node<Any>>(), any()) } returns emptyList()
    }

    @Test
    fun `cloning applies the original speed to the mean delay of the new reaction`() {
        val clone = strategy(reactionWith { MEAN_DELAY }).cloneIfNeeded(mockk(), reactionWith { 2 * MEAN_DELAY })
        assertEquals(SPEED * 2 * MEAN_DELAY, clone.getNodeMovementLength(null))
    }

    @Test
    fun `the movement length fails when the mean delay is not finite and positive`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0).forEach { invalid ->
            assertFailsWith<IllegalStateException> {
                strategy(reactionWith { invalid }).getNodeMovementLength(null)
            }
        }
    }

    @Test
    fun `the movement length follows the current mean delay of the reaction`() {
        var meanDelay = MEAN_DELAY
        val strategy = strategy(reactionWith { meanDelay })
        meanDelay *= 2
        assertEquals(SPEED * meanDelay, strategy.getNodeMovementLength(null))
    }

    @Test
    fun `without interacting neighbors the node covers its speed times the mean delay of the reaction`() {
        assertEquals(SPEED * MEAN_DELAY, strategy(reactionWith { MEAN_DELAY }).getNodeMovementLength(null))
    }

    private fun reactionWith(meanDelay: () -> Double): NodeReaction<Any> = mockk<TimedNodeReaction> {
        every { this@mockk.meanDelay } answers { meanDelay() }
        every { finitePositiveMeanDelayOrError } answers { callOriginal() }
    }

    private fun strategy(reaction: NodeReaction<Any>): InteractWithOthers<Any, Euclidean2DPosition> =
        InteractWithOthers(environment, mockk(), reaction, SimpleMolecule("crowd"), SPEED, 1.0, 1.0)

    private interface TimedNodeReaction :
        NodeReaction<Any>,
        TimeDistributedReaction<Any>

    private companion object {
        private const val MEAN_DELAY = 0.5
        private const val SPEED = 10.0
    }
}
