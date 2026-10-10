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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NodePropertiesTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    private fun propertyOf(node: Node<Any>): NodeProperty<Any> = mockk {
        every { this@mockk.node } returns node
        every { cloneOnNewNode(any()) } answers { propertyOf(firstArg()) }
    }

    @Test
    fun `properties are added during the node setup`() {
        val node = GenericNode(environment)
        val property = propertyOf(node)
        node.addProperty(property)
        environment.addNode(node, environment.makePosition(0, 0))
        assertEquals(listOf<NodeProperty<Any>>(property), node.properties)
    }

    @Test
    fun `properties cannot be added once the node is in the environment`() {
        val node = GenericNode(environment)
        environment.addNode(node, environment.makePosition(0, 0))
        assertFailsWith<IllegalStateException> { node.addProperty(propertyOf(node)) }
        assertTrue(node.properties.isEmpty())
    }

    @Test
    fun `cloned nodes receive the properties of the original`() {
        val node = GenericNode(environment).apply { addProperty(propertyOf(this)) }
        environment.addNode(node, environment.makePosition(0, 0))
        val clone = node.cloneNode(Time.ZERO)
        assertFalse(clone in environment)
        assertEquals(clone, clone.properties.single().node)
    }
}
