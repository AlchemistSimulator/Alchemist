package it.unibo.alchemist.test

import io.kotlintest.TestCase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.CircleNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

class TestEuclideanPhysics2DEnvironment : StringSpec() {
    private lateinit var env: EuclideanPhysics2DEnvironment<Any>
    private lateinit var node1: Node<Any>
    private lateinit var node2: Node<Any>
    private lateinit var node3: Node<Any>

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        env = Continuous2DEnvironment()
        env.linkingRule = NoLinks()
        node1 = CircleNode(env, DEFAULT_SHAPE_SIZE / 2)
        node2 = CircleNode(env, DEFAULT_SHAPE_SIZE / 2)
        node3 = CircleNode(env, DEFAULT_SHAPE_SIZE / 2)
    }

    init {
        "Cannot add overlapping nodes" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node3, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            env.nodes shouldContainExactlyInAnyOrder listOf(node1, node3)
        }

        "Cannot move into other nodes" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0))
            env.moveNodeToPosition(node2, env.getPosition(node1))
            env.getPosition(node1)!!.getDistanceTo(env.getPosition(node2)) shouldBeGreaterThanOrEqual
                (env.getShape(node1).diameter + env.getShape(node2).diameter) / 2
        }

        "Get nodes within a small shape" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(2 * DEFAULT_SHAPE_SIZE, 0.0))
            val shape = env.shapeFactory.rectangle(DEFAULT_SHAPE_SIZE / 2, DEFAULT_SHAPE_SIZE / 2)
            env.getNodesWithin(shape) shouldContainExactly listOf(node1)
        }

        "Get nodes within a big shape" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(2 * DEFAULT_SHAPE_SIZE, 0.0))
            env.addNode(node3, Euclidean2DPosition(30 * DEFAULT_SHAPE_SIZE, 0.0))
            val shape = env.shapeFactory.rectangle(3.1 * DEFAULT_SHAPE_SIZE, DEFAULT_SHAPE_SIZE)
            env.getNodesWithin(shape) shouldContainExactlyInAnyOrder listOf(node1, node2)
        }
    }
}
