package it.unibo.alchemist.test

import io.kotlintest.TestCase
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.implementations.environments.Physics2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.CircleNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition

private const val SIZE = 1.0

class TestAddingOverlappingNodes : StringSpec() {
    private lateinit var env: Physics2DEnvironment<Any>
    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        env = Physics2DEnvironment()
        env.linkingRule = NoLinks()
    }

    private fun makeNode(size: Double = SIZE) = CircleNode(env, size / 2)

    init {
        "Cannot add overlapping nodes" {
            val node1 = makeNode()
            val node2 = makeNode()
            val node3 = makeNode()
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node3, Euclidean2DPosition(3 * SIZE, 0.0))
            env.nodes shouldContain node1
            env.nodes shouldContain node3
            env.nodes shouldNotContain node2
        }

        "Cannot move into other nodes" {
            val node1 = makeNode()
            val node2 = makeNode()
            env.addNode(node1, Euclidean2DPosition(0.0, 0.0))
            env.addNode(node2, Euclidean2DPosition(3 * SIZE, 0.0))
            env.moveNodeToPosition(node2, env.getPosition(node1)!!)

            env.getPosition(node1)!!.getDistanceTo(env.getPosition(node2)!!) shouldBeGreaterThanOrEqual
                (env.getShape(node1).diameter + env.getShape(node2).diameter) / 2
        }
    }
}