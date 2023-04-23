/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.properties.CircularArea
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.AreaProperty
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.test.TestEuclidean2DShapeFactory.Companion.DEFAULT_SHAPE_SIZE
import org.danilopianini.lang.MathUtils

internal infix fun Double.shouldBeAbout(other: Double) = MathUtils.fuzzyEquals(this, other) shouldBe true

class TestEuclideanPhysics2DEnvironment : StringSpec() {
    private lateinit var environment: Physics2DEnvironment<Any>
    private lateinit var node1: Node<Any>
    private lateinit var node2: Node<Any>
    private lateinit var node3: Node<Any>

    private fun createCircleNode(
        incarnation: Incarnation<Any, Euclidean2DPosition>,
        environment: Physics2DEnvironment<Any>,
        radius: Double,
    ) = GenericNode(incarnation, environment).apply {
        addProperty(CircularArea(environment, this, radius))
    }

    private fun getNodeRadius(node: Node<Any>): Double =
        node.asProperty<Any, AreaProperty<Any>>().shape.radius

    override suspend fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        environment = Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").get())
        val incarnation = SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()
        environment.linkingRule = NoLinks()
        node1 = createCircleNode(incarnation, environment, DEFAULT_SHAPE_SIZE / 2)
        node2 = createCircleNode(incarnation, environment, DEFAULT_SHAPE_SIZE / 2)
        node3 = createCircleNode(incarnation, environment, DEFAULT_SHAPE_SIZE / 2)
    }

    init {
        "Cannot add overlapping nodes" {
            environment.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            environment.addNode(node2, Euclidean2DPosition(0.0, 0.0))
            environment.addNode(node3, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            environment.nodes shouldContainExactlyInAnyOrder listOf(node1, node3)
        }

        "Cannot move into other nodes" {
            environment.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            environment.addNode(node2, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            environment.moveNodeToPosition(node2, environment.getPosition(node1))
            val distance = environment.getPosition(node1).distanceTo(environment.getPosition(node2))
            distance shouldBeAbout getNodeRadius(node1) + getNodeRadius(node2)
        }

        "Get nodes within a small shape" {
            environment.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            environment.addNode(node2, Euclidean2DPosition(2 * DEFAULT_SHAPE_SIZE, 0.0))
            val shape = environment.shapeFactory.rectangle(DEFAULT_SHAPE_SIZE / 2, DEFAULT_SHAPE_SIZE / 2)
            environment.getNodesWithin(shape) shouldContainExactly listOf(node1)
        }

        "Get nodes within a big shape" {
            environment.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            environment.addNode(node2, Euclidean2DPosition(2 * DEFAULT_SHAPE_SIZE, 0.0))
            environment.addNode(node3, Euclidean2DPosition(30 * DEFAULT_SHAPE_SIZE, 0.0))
            val shape = environment.shapeFactory.rectangle(3.1 * DEFAULT_SHAPE_SIZE, DEFAULT_SHAPE_SIZE)
            environment.getNodesWithin(shape) shouldContainExactlyInAnyOrder listOf(node1, node2)
        }

        "Node is moved to the farthest position reachable when its path is occupied by others" {
            environment.addNode(node1, coords(2.0, 2.0))
            environment.addNode(node2, coords(6.0, 2.0))
            val target = coords(8.0, 2.0)
            environment.moveNodeToPosition(node1, target)
            environment.getPosition(node1).distanceTo(target) shouldBeAbout
                environment.getPosition(node2).distanceTo(target) + getNodeRadius(node1) + getNodeRadius(node2)
        }

        "Node is moved to the farthest position reachable when its path is occupied by others 2" {
            environment.addNode(node1, coords(2.0, 2.0))
            environment.addNode(node2, coords(8.0, 1.0))
            environment.addNode(node3, coords(8.0, 2.5))
            val target = coords(8.0, 2.0)
            environment.moveNodeToPosition(node1, target)
            environment.getPosition(node1).distanceTo(target) shouldBeGreaterThan getNodeRadius(node1)
        }
    }
}
