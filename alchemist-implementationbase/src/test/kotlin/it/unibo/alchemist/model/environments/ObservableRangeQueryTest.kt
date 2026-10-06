/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableRangeQueryTest {

    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    private fun nodeAt(x: Number, y: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }

    @Test
    fun `a position-centered range tracks nodes entering and leaving it`() {
        val inside = nodeAt(0.5, 0)
        val outside = nodeAt(5, 0)
        val visible = environment.observeNodesWithinRange(environment.makePosition(0, 0), 1.0)
        val subscription = visible.subscribe { }
        assertEquals(setOf(inside), visible.current)
        environment.moveNodeToPosition(outside, environment.makePosition(0, 0.5))
        environment.moveNodeToPosition(inside, environment.makePosition(5, 5))
        assertEquals(setOf(outside), visible.current)
        subscription.dispose()
    }

    @Test
    fun `a node-centered range follows its center when the center moves`() {
        val center = nodeAt(0, 0)
        val near = nodeAt(0.5, 0)
        val far = nodeAt(10, 0)
        val visible = environment.observeNodesWithinRange(center, 1.0)
        val subscription = visible.subscribe { }
        assertEquals(setOf(near), visible.current)
        environment.moveNodeToPosition(center, environment.makePosition(10, 0.5))
        assertEquals(setOf(far), visible.current)
        environment.moveNodeToPosition(center, environment.makePosition(10, 0.2))
        assertEquals(setOf(far), visible.current)
        subscription.dispose()
    }

    @Test
    fun `removing the center of an observed range keeps the other observed ranges consistent`() {
        val removedCenter = nodeAt(0, 0)
        val survivingCenter = nodeAt(10, 0)
        val member = nodeAt(10, 0.5)
        val removedRange = environment.observeNodesWithinRange(removedCenter, 1.0)
        val survivingRange = environment.observeNodesWithinRange(survivingCenter, 1.0)
        val subscriptions = listOf(removedRange.subscribe { }, survivingRange.subscribe { })
        assertEquals(setOf(member), survivingRange.current)
        environment.removeNode(removedCenter)
        environment.moveNodeToPosition(member, environment.makePosition(20, 0))
        assertEquals(emptySet(), survivingRange.current)
        environment.moveNodeToPosition(member, environment.makePosition(10, 0.5))
        assertEquals(setOf(member), survivingRange.current)
        subscriptions.forEach { it.dispose() }
    }
}
