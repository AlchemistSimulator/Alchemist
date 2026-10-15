/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.nodes

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.Test

class NodeCloningTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    @Test
    fun `reactions are cloned at the simulation time`() {
        val now = DoubleTime(5.0)
        environment.simulation = mockk<Simulation<Any, Euclidean2DPosition>> { every { time } returns now }
        val reaction = reactionOn(GenericNode(environment))
        reaction.host.cloneNode()
        verify(exactly = 1) { reaction.cloneOnNewNode(any(), now) }
    }

    @Test
    fun `reactions are cloned at time zero without a simulation`() {
        val reaction = reactionOn(GenericNode(environment))
        reaction.host.cloneNode()
        verify(exactly = 1) { reaction.cloneOnNewNode(any(), Time.ZERO) }
    }

    private fun reactionOn(node: Node<Any>): NodeReaction<Any> = mockk<NodeReaction<Any>>(relaxed = true) {
        every { host } returns node
        every { cloneOnNewNode(any(), any()) } answers { reactionOn(firstArg()) }
    }.also(node::addReaction)
}
