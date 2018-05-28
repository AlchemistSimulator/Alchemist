package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.implementations.nodes.IntNode
import org.junit.Assert
import org.junit.Test

class TestNeighborhood {

    @Test
    fun testClone() {
        val env = Continuous2DEnvironment<Int>()
        val n1 = IntNode(env)
        val n2 = IntNode(env)
        val neigh1 = Neighborhoods.make(env, n1, mutableListOf(n2))
        val neigh2 = neigh1.clone()
        neigh1.removeNeighbor(n2)
        Assert.assertEquals(1, neigh2.size())
        Assert.assertTrue(neigh2.neighbors.contains(n2))
    }
}