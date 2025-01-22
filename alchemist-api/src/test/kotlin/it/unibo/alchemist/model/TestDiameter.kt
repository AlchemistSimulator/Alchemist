/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.physics.environments.ContinuousPhysics2DEnvironment
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.physics.properties.CircularArea
import it.unibo.alchemist.model.positions.Euclidean2DPosition

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

    private fun addNodeToEnv(vararg nodePos: Pair<Node<Any>, Euclidean2DPosition>) {
        nodePos.forEach { (node, position) ->
            env.addNode(node, position)
        }
    }

    private fun verifyUnifiedNetworkDiameter(
        expected: Int,
        vararg nodes: Node<Any>,
    ) {
        env.isNetworkSegmented shouldBe false
        env.nodes.size shouldBe nodes.size
        nodes.forEach { node ->
            env.networkDiameter(node) shouldBe expected
        }
        env.allDiameters().forEach { subnetwork ->
            subnetwork.diameter shouldBe expected
        }
    }

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
            addNodeToEnv(node1 to Euclidean2DPosition(0.0, 0.0))
            verifyUnifiedNetworkDiameter(expected = 0, node1)
        }

        "Two connected nodes increase the diameter of the network to 1" {
            addNodeToEnv(
                node1 to Euclidean2DPosition(0.0, 0.0),
                node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            )
            verifyUnifiedNetworkDiameter(expected = 1, node1, node2)
        }

        "With three fully connected nodes the maximum distance between two nodes is still 1" {
            addNodeToEnv(
                node1 to Euclidean2DPosition(0.0, 0.0),
                node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
                node3 to Euclidean2DPosition(0.0, 3 * DEFAULT_SHAPE_SIZE),
            )
            verifyUnifiedNetworkDiameter(expected = 1, node1, node2, node3)
        }

        "A network with three nodes with just one neighbor each, has 2 as diameter" {
            addNodeToEnv(
                node1 to Euclidean2DPosition(0.0, 0.0),
                node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
                node3 to Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0),
            )
            verifyUnifiedNetworkDiameter(expected = 2, node1, node2, node3)
        }

        "A four connected nodes network maintains a diameter of 2" {
            addNodeToEnv(
                node1 to Euclidean2DPosition(0.0, 0.0),
                node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
                node3 to Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0),
                node4 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 2 * DEFAULT_SHAPE_SIZE),
            )
            verifyUnifiedNetworkDiameter(expected = 2, node1, node2, node3, node4)
        }

        "A network of three nodes where one node is isolated should have different diameters" {
            addNodeToEnv(
                node1 to Euclidean2DPosition(0.0, 0.0),
                node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
                node3 to Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 0.0),
            )
            env.nodes.size shouldBe 3
            env.isNetworkSegmented shouldBe true
            env.networkDiameter(node1) shouldBe 1
            env.networkDiameter(node2) shouldBe 1
            env.networkDiameter(node3) shouldBe 0
            val subnetworks = env.allDiameters()
            subnetworks shouldHaveSize 2
            subnetworks.firstOrNull { sub -> sub.contains(node1) }?.diameter shouldBe 1
            subnetworks.firstOrNull { sub -> sub.contains(node3) }?.diameter shouldBe 0
        }
    }

    companion object {
        private const val DEFAULT_SHAPE_SIZE: Double = 1.0
    }
}
