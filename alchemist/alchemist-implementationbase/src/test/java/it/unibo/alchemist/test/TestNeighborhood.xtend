package it.unibo.alchemist.test

import org.junit.Test
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.nodes.IntNode
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import org.junit.Assert

class TestNeighborhood {
	
	@Test
	def void testClone() {
		val env = new Continuous2DEnvironment<Integer>
		val n1 = new IntNode(env)
		val n2 = new IntNode(env)
		val neigh1 = Neighborhoods.make(env, n1, #[n2])
		val neigh2 = neigh1.remove(n2)
		Assert.assertEquals(0, neigh2.size)
		Assert.assertTrue(neigh1.neighbors.contains(n2))
	}
	
}