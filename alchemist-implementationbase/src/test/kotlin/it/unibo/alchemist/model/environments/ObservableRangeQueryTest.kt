/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import arrow.core.None
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observables.util.ObservableSets.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableRangeQueryTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    @Test
    fun `nodes exactly at the queried range along an axis are within it`() {
        val center = nodeAt(-2.5, 0)
        val atRange = nodeAt(1.5, 0)
        assertEquals(listOf(atRange), environment.nodesAround(center, 4.0))
    }

    @Test
    fun `a node leaving a range for a far position is removed from it`() {
        val center = nodeAt(0, 0)
        val member = nodeAt(0.5, 0)
        val range = environment.observeNodesAround(center, 1.0)
        val subscription = range.onChange { }
        environment.moveNodeTo(member, environment.makePosition(100, 100))
        assertEquals(emptySet(), range.current)
        subscription.dispose()
    }

    @Test
    fun `a node moving within a large range joins it even far from smaller ranges`() {
        val small = environment.observeNodesAround(nodeAt(0, 0), 1.0)
        val largeCenter = nodeAt(20, 0)
        val large = environment.observeNodesAround(largeCenter, 5.0)
        val subscriptions = listOf(small.onChange { }, large.onChange { })
        val mover = nodeAt(10, 0)
        environment.moveNodeTo(mover, environment.makePosition(16, 0))
        assertEquals(emptySet(), small.current)
        assertEquals(setOf(mover), large.current)
        subscriptions.forEach { it.dispose() }
    }

    @Test
    fun `a node-centered range follows its center when the center moves`() {
        val center = nodeAt(0, 0)
        val near = nodeAt(0.5, 0)
        val far = nodeAt(10, 0)
        val visible = environment.observeNodesAround(center, 1.0)
        val subscription = visible.onChange { }
        assertEquals(setOf(near), visible.current)
        environment.moveNodeTo(center, environment.makePosition(10, 0.5))
        assertEquals(setOf(far), visible.current)
        environment.moveNodeTo(center, environment.makePosition(10, 0.2))
        assertEquals(setOf(far), visible.current)
        subscription.dispose()
    }

    @Test
    fun `a position-centered range tracks nodes entering and leaving it`() {
        val inside = nodeAt(0.5, 0)
        val outside = nodeAt(5, 0)
        val visible = environment.observeNodesAround(environment.makePosition(0, 0), 1.0)
        val subscription = visible.onChange { }
        assertEquals(setOf(inside), visible.current)
        environment.moveNodeTo(outside, environment.makePosition(0, 0.5))
        environment.moveNodeTo(inside, environment.makePosition(5, 5))
        assertEquals(setOf(outside), visible.current)
        subscription.dispose()
    }

    @Test
    fun `a range unobserved when its center is removed is empty`() {
        val center = nodeAt(0, 0)
        nodeAt(0.5, 0)
        val range = environment.observeNodesAround(center, 1.0)
        environment.removeNode(center)
        assertEquals(emptySet(), range.current)
        val received = mutableListOf<Set<Node<Any>>>()
        val subscription = range.onChange { received += it }
        assertEquals(listOf(emptySet()), received)
        subscription.dispose()
    }

    @Test
    fun `a range observed again after its center is removed is empty`() {
        val center = nodeAt(0, 0)
        nodeAt(0.5, 0)
        val range = environment.observeNodesAround(center, 1.0)
        range.onChange { }.dispose()
        environment.removeNode(center)
        val received = mutableListOf<Set<Node<Any>>>()
        val subscription = range.onChange { received += it }
        assertEquals(listOf(emptySet()), received)
        subscription.dispose()
    }

    @Test
    fun `a size subscription of a range can be released`() {
        val center = nodeAt(0, 0)
        val range = environment.observeNodesAround(center, 1.0)
        val sizes = mutableListOf<Int>()
        range.size.onChange(notifyOnRegistration = false) { sizes += it }.dispose()
        nodeAt(0.5, 0)
        assertEquals(emptyList(), sizes)
    }

    @Test
    fun `observers of a range can read the neighborhood of a node added within it`() {
        with(environment) { networkModel = connectWithinDistance(1.0) }
        val neighbor = nodeAt(0.2, 0)
        val neighborsOfMembers = mutableListOf<List<Node<Any>>>()
        val subscription = environment.observeNodesAround(environment.makePosition(0, 0), 1.0)
            .onChange(notifyOnRegistration = false) { members ->
                members.filter { it != neighbor }.forEach {
                    neighborsOfMembers += environment.neighborhoodOf(it).current.neighbors.toList()
                }
            }
        nodeAt(0.5, 0)
        assertEquals(listOf(listOf(neighbor)), neighborsOfMembers)
        subscription.dispose()
    }

    @Test
    fun `observers of a range find a node added within it through spatial queries`() {
        val center = nodeAt(0, 0)
        val queried = mutableListOf<List<Node<Any>>>()
        val subscription = environment.observeNodesAround(center, 1.0)
            .onChange(notifyOnRegistration = false) { queried += environment.nodesAround(center, 1.0) }
        val added = nodeAt(0.5, 0)
        assertEquals(listOf(listOf(added)), queried)
        subscription.dispose()
    }

    @Test
    fun `releasing the largest range keeps the smaller ranges up to date`() {
        val center = nodeAt(0, 0)
        val small = environment.observeNodesAround(center, 1.0)
        val smallSubscription = small.onChange { }
        environment.observeNodesAround(nodeAt(50, 0), 10.0).onChange { }.dispose()
        val mover = nodeAt(5, 0)
        environment.moveNodeTo(mover, environment.makePosition(0.5, 0))
        assertEquals(setOf(mover), small.current)
        environment.moveNodeTo(mover, environment.makePosition(0.5, 0))
        assertEquals(setOf(mover), small.current)
        environment.moveNodeTo(mover, environment.makePosition(5, 0))
        assertEquals(emptySet(), small.current)
        smallSubscription.dispose()
    }

    @Test
    fun `removing a node observed through a neighborhood is safe`() {
        with(environment) { networkModel = connectWithinDistance(1.0) }
        val center = nodeAt(0, 0)
        val near = nodeAt(0.5, 0)
        val positions = environment.neighborhoodOf(center).switchMap { neighborhood ->
            neighborhood.neighbors.map(environment::positionOf).combineLatest { it }
        }
        val subscription = positions.onChange { }
        environment.removeNode(near)
        assertEquals(None, positions.current)
        subscription.dispose()
    }

    @Test
    fun `removing a node observed through a range query is safe`() {
        val center = nodeAt(0, 0)
        val near = nodeAt(0.5, 0)
        val positions = environment.observeNodesAround(center, 1.0)
            .combineLatest(environment::positionOf) { it.toList() }
        val subscription = positions.onChange { }
        environment.removeNode(near)
        assertEquals(emptyList(), positions.current)
        subscription.dispose()
    }

    @Test
    fun `removing the center of an observed range keeps the other observed ranges consistent`() {
        val removedCenter = nodeAt(0, 0)
        val survivingCenter = nodeAt(10, 0)
        val member = nodeAt(10, 0.5)
        val removedRange = environment.observeNodesAround(removedCenter, 1.0)
        val survivingRange = environment.observeNodesAround(survivingCenter, 1.0)
        val subscriptions = listOf(removedRange.onChange { }, survivingRange.onChange { })
        assertEquals(setOf(member), survivingRange.current)
        environment.removeNode(removedCenter)
        environment.moveNodeTo(member, environment.makePosition(20, 0))
        assertEquals(emptySet(), survivingRange.current)
        environment.moveNodeTo(member, environment.makePosition(10, 0.5))
        assertEquals(setOf(member), survivingRange.current)
        subscriptions.forEach { it.dispose() }
    }

    private fun nodeAt(x: Number, y: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }
}
