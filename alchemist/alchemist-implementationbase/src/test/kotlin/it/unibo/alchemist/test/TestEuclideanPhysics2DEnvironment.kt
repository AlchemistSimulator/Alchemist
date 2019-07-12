package it.unibo.alchemist.test

import io.kotlintest.TestCase
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.implementations.environments.EuclideanPhysics2DEnvironmentImpl
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.CircleNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node

private const val NODE_SIZE = 1.0

class TestEuclideanPhysics2DEnvironment : StringSpec() {
    private lateinit var env: EuclideanPhysics2DEnvironmentImpl<Any>
    private lateinit var node1: Node<Any>
    private lateinit var node2: Node<Any>
    private lateinit var node3: Node<Any>

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        env = EuclideanPhysics2DEnvironmentImpl()
        env.linkingRule = NoLinks()
        node1 = CircleNode(env, NODE_SIZE / 2)
        node2 = CircleNode(env, NODE_SIZE / 2)
        node3 = CircleNode(env, NODE_SIZE / 2)
    }

    init {
        "Cannot add overlapping nodes" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node3, Euclidean2DPosition(3 * NODE_SIZE, 0.0))
            env.nodes shouldContainExactlyInAnyOrder listOf(node1, node3)
        }

        "Cannot move into other nodes" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * NODE_SIZE, 0.0))
            env.moveNodeToPosition(node2, env.getPosition(node1)!!)
            env.getPosition(node1)!!.getDistanceTo(env.getPosition(node2)!!) shouldBeGreaterThanOrEqual
                (env.getShape(node1).diameter + env.getShape(node2).diameter) / 2
        }

        "Get nodes within a shape" {
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * NODE_SIZE, 0.0))
            env.addNode(node3, Euclidean2DPosition(30 * NODE_SIZE, 0.0))
            val shape = env.shapeFactory.rectangle(5.1 * NODE_SIZE, NODE_SIZE / 2)
            env.getNodesWithin(shape) shouldContainExactlyInAnyOrder listOf(node1, node2)
        }
    }
}