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

class InteractWithOthersTest {
    private val environment: Environment<Any, Euclidean2DPosition> = mockk {
        every { getNodesWithinRange(any<Node<Any>>(), any()) } returns emptyList()
    }

    private interface TimedNodeReaction :
        NodeReaction<Any>,
        TimeDistributedReaction<Any>

    private fun reactionWithRate(rate: Double): NodeReaction<Any> = mockk<TimedNodeReaction> {
        every { this@mockk.rate } returns rate
    }

    private fun strategy(rate: Double): InteractWithOthers<Any, Euclidean2DPosition> = InteractWithOthers(
        environment,
        mockk(),
        reactionWithRate(rate),
        SimpleMolecule("crowd"),
        SPEED,
        1.0,
        1.0,
    )

    @Test
    fun `without interacting neighbors the node moves at its speed divided by the reaction rate`() {
        assertEquals(SPEED / RATE, strategy(RATE).getNodeMovementLength(null))
    }

    @Test
    fun `cloning applies the original speed to the rate of the new reaction`() {
        val clone = strategy(RATE).cloneIfNeeded(mockk(), reactionWithRate(RATE))
        assertEquals(SPEED / RATE, clone.getNodeMovementLength(null))
    }

    private companion object {
        private const val SPEED = 10.0
        private const val RATE = 2.0
    }
}
