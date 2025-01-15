/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.physics.properties.AreaProperty
import it.unibo.alchemist.model.physics.properties.CircularArea
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.Doubles.fuzzyEquals

private infix fun Double.shouldBeAbout(other: Double) = fuzzyEquals(other) shouldBe true

class TestDiameter : StringSpec() {
    private lateinit var env: Physics2DEnvironment<Any>
    private lateinit var node1: Node<Any>
    private lateinit var node2: Node<Any>
    private lateinit var node3: Node<Any>
    private lateinit var node4: Node<Any>

    private fun createCircleNode(
        incarnation: Incarnation<Any, Euclidean2DPosition>,
        environment: Physics2DEnvironment<Any>,
        radius: Double,
    ) = GenericNode(incarnation, environment).apply {
        addProperty(CircularArea(environment, this, radius))
    }

    private fun getNodeRadius(node: Node<Any>): Double = node.asProperty<Any, AreaProperty<Any>>().shape.radius

    override suspend fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        val incarnation = SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()
        env = ContinuousPhysics2DEnvironment(incarnation)
        env.linkingRule = ConnectWithinDistance(5.0)
        node1 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
        node2 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
        node3 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
        node4 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
    }

    init {
        "An environment with just one node has diameter as 0" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.nodes.size shouldBe 1
            env.networkDiameter shouldBe 0
        }

        "Two connected nodes increase the diameter of the network to 1" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            env.nodes.size shouldBe 2
            env.networkDiameter shouldBe 1
        }

        "With thre fully connected nodes the maximum distance between two nodes is still 1" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            env.addNode(node3, Euclidean2DPosition(0.0, 3 * DEFAULT_SHAPE_SIZE))
            env.nodes.size shouldBe 3
            env.networkDiameter shouldBe 1
        }

        "A network with three nodes with just one neighbor each, has 2 as diameter" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            env.addNode(node3, Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0))
            env.nodes.size shouldBe 3
            env.networkDiameter shouldBe 2
        }

        "A four-node network maintains a diameter of 2" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            env.addNode(node3, Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0))
            env.addNode(node4, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 2 * DEFAULT_SHAPE_SIZE))
            env.nodes.size shouldBe 4
            env.networkDiameter shouldBe 2
        }

        "A network of three nodes where one node is isolated has a diameter of 1" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            env.addNode(node3, Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 0.0))
            env.nodes.size shouldBe 3
            env.networkDiameter shouldBe 1
        }
    }

    companion object {
        private const val DEFAULT_SHAPE_SIZE: Double = 1.0
    }
}
