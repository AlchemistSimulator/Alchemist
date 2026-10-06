/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import io.mockk.mockk
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ObservableLayerTest {

    private fun environment() =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    @Test
    fun `a time-invariant layer value follows the observed node across a spatial gradient`() {
        val environment = environment()
        environment.addLayer(LAYER, StepLayer(0.0, 0.0, HIGH, LOW))
        val node = GenericNode(environment)
        environment.addNode(node, environment.makePosition(-1, -1))
        assertNull(environment.observeLayerValue(SimpleMolecule("absent"), node).current)
        val value = environment.observeLayerValue(LAYER, node)
        val received = mutableListOf<Any?>()
        val subscription = value.subscribe(invokeOnSubscription = false) { received += it }
        environment.moveNodeToPosition(node, environment.makePosition(-2, -2))
        assertEquals(emptyList(), received)
        environment.moveNodeToPosition(node, environment.makePosition(1, 1))
        assertEquals(listOf<Any?>(HIGH), received)
        environment.moveNodeToPosition(node, environment.makePosition(1, -1))
        assertEquals(listOf<Any?>(HIGH, LOW), received)
        assertEquals(LOW, value.current)
        subscription.dispose()
        environment.moveNodeToPosition(node, environment.makePosition(2, 2))
        assertEquals(listOf<Any?>(HIGH, LOW), received)
    }

    @Test
    fun `a time-varying layer notifies changes at the observed position until unsubscribed`() {
        val environment = environment()
        val layer = ScaledLayer(observe(1.0))
        environment.addLayer(LAYER, layer)
        val node = GenericNode(environment)
        environment.addNode(node, environment.makePosition(2, 0))
        val value = environment.observeLayerValue(LAYER, node)
        val received = mutableListOf<Any?>()
        val subscription = value.subscribe { received += it }
        layer.scale.current = 3.0
        environment.moveNodeToPosition(node, environment.makePosition(1, 0))
        assertEquals(listOf<Any?>(2.0, 6.0, 3.0), received)
        subscription.dispose()
        layer.scale.current = 5.0
        assertEquals(listOf<Any?>(2.0, 6.0, 3.0), received)
        assertEquals(0, layer.scale.observers.size)
    }

    @Test
    fun `a layer value observed during setup resolves a layer associated later`() {
        val environment = environment()
        val node = GenericNode(environment)
        environment.addNode(node, environment.makePosition(1, 1))
        val value = environment.observeLayerValue(LAYER, node)
        assertNull(value.current)
        environment.addLayer(LAYER, StepLayer(0.0, 0.0, HIGH, LOW))
        assertEquals(HIGH, value.current)
    }

    @Test
    fun `layers cannot be associated once the environment joins a simulation`() {
        val environment = environment()
        environment.simulation = mockk(relaxed = true)
        assertFailsWith<IllegalStateException> { environment.addLayer(LAYER, StepLayer(HIGH, LOW)) }
        assertNull(environment.getLayer(LAYER))
    }

    /**
     * A layer whose value at a position is its x coordinate multiplied by a time-varying [scale].
     */
    private class ScaledLayer(val scale: MutableObservable<Double>) :
        Layer<Any, Euclidean2DPosition> {
        override fun getValue(position: Euclidean2DPosition): Any = position.x * scale.current

        override fun observeValue(position: Euclidean2DPosition): Observable<Any> = scale.map { position.x * it }
    }

    private companion object {
        val LAYER = SimpleMolecule("layer")
        const val HIGH = "high"
        const val LOW = "low"
    }
}
