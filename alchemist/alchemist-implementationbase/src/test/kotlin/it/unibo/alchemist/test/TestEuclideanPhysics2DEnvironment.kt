package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.CircleNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import org.danilopianini.lang.MathUtils

internal infix fun Double.shouldBeFuzzy(other: Double): Unit = MathUtils.fuzzyEquals(this, other) shouldBe true

class TestEuclideanPhysics2DEnvironment : StringSpec() {
    private lateinit var env: Physics2DEnvironment<Any>
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
            env.getPosition(node1).distanceTo(env.getPosition(node2)) shouldBeFuzzy
                node1.shape.radius + node2.shape.radius
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

        "Node is moved to the farthest position reachable when its path is occupied by others" {
            env.addNode(node1, coords(2.0, 2.0))
            env.addNode(node2, coords(6.0, 2.0))
            val target = coords(8.0, 2.0)
            env.moveNodeToPosition(node1, target)
            env.getPosition(node1).distanceTo(target) shouldBeFuzzy
                env.getPosition(node2).distanceTo(target) + node1.shape.radius + node2.shape.radius
        }

        "Node is moved to the farthest position reachable when its path is occupied by others 2" {
            env.addNode(node1, coords(2.0, 2.0))
            env.addNode(node2, coords(8.0, 1.0))
            env.addNode(node3, coords(8.0, 2.5))
            val target = coords(8.0, 2.0)
            env.moveNodeToPosition(node1, target)
            env.getPosition(node1).distanceTo(target) shouldBeGreaterThan node1.shape.radius
        }
    }
}
