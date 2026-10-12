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
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
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

    private fun nodeAt(x: Number, y: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }

    @Test
    fun `removing a node observed through a range query is safe`() {
        val center = nodeAt(0, 0)
        val near = nodeAt(0.5, 0)
        val positions = environment.observeNodesWithinRange(center, 1.0)
            .combineLatest(environment::getPosition) { it.toList() }
        val subscription = positions.subscribe { }
        environment.removeNode(near)
        assertEquals(emptyList(), positions.current)
        subscription.dispose()
    }

    @Test
    fun `removing a node observed through a neighborhood is safe`() {
        environment.linkingRule = ConnectWithinDistance(1.0)
        val center = nodeAt(0, 0)
        val near = nodeAt(0.5, 0)
        val positions = environment.getNeighborhood(center).switchMap { neighborhood ->
            neighborhood.neighbors.map(environment::getPosition).combineLatest { it }
        }
        val subscription = positions.subscribe { }
        environment.removeNode(near)
        assertEquals(None, positions.current)
        subscription.dispose()
    }

    @Test
    fun `a position-centered range tracks nodes entering and leaving it`() {
        val inside = nodeAt(0.5, 0)
        val outside = nodeAt(5, 0)
        val visible = environment.observeNodesWithinRange(environment.makePosition(0, 0), 1.0)
        val subscription = visible.subscribe { }
        assertEquals(setOf(inside), visible.current)
        environment.moveNodeTo(outside, environment.makePosition(0, 0.5))
        environment.moveNodeTo(inside, environment.makePosition(5, 5))
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
        environment.moveNodeTo(center, environment.makePosition(10, 0.5))
        assertEquals(setOf(far), visible.current)
        environment.moveNodeTo(center, environment.makePosition(10, 0.2))
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
        environment.moveNodeTo(member, environment.makePosition(20, 0))
        assertEquals(emptySet(), survivingRange.current)
        environment.moveNodeTo(member, environment.makePosition(10, 0.5))
        assertEquals(setOf(member), survivingRange.current)
        subscriptions.forEach { it.dispose() }
    }

    @Test
    fun `observers of a range find a node added within it through spatial queries`() {
        val center = nodeAt(0, 0)
        val queried = mutableListOf<List<Node<Any>>>()
        val subscription = environment.observeNodesWithinRange(center, 1.0)
            .subscribe(invokeOnSubscription = false) { queried += environment.getNodesWithinRange(center, 1.0) }
        val added = nodeAt(0.5, 0)
        assertEquals(listOf(listOf(added)), queried)
        subscription.dispose()
    }

    @Test
    fun `a range unobserved when its center is removed is empty`() {
        val center = nodeAt(0, 0)
        nodeAt(0.5, 0)
        val range = environment.observeNodesWithinRange(center, 1.0)
        environment.removeNode(center)
        assertEquals(emptySet(), range.current)
        val received = mutableListOf<Set<Node<Any>>>()
        val subscription = range.subscribe { received += it }
        assertEquals(listOf(emptySet()), received)
        subscription.dispose()
    }

    @Test
    fun `a size subscription of a range can be released through the size of the same range`() {
        val center = nodeAt(0, 0)
        val range = environment.observeNodesWithinRange(center, 1.0)
        val sizes = mutableListOf<Int>()
        range.size.onChange(this, invokeOnRegistration = false) { sizes += it }
        range.size.stopWatching(this)
        nodeAt(0.5, 0)
        assertEquals(emptyList(), sizes)
    }

    @Test
    fun `observers of a range can read the neighborhood of a node added within it`() {
        environment.linkingRule = ConnectWithinDistance(1.0)
        val neighbor = nodeAt(0.2, 0)
        val neighborsOfMembers = mutableListOf<List<Node<Any>>>()
        val subscription = environment.observeNodesWithinRange(environment.makePosition(0, 0), 1.0)
            .subscribe(invokeOnSubscription = false) { members ->
                members.filter { it != neighbor }.forEach {
                    neighborsOfMembers += environment.getNeighborhood(it).current.neighbors.toList()
                }
            }
        nodeAt(0.5, 0)
        assertEquals(listOf(listOf(neighbor)), neighborsOfMembers)
        subscription.dispose()
    }
}
