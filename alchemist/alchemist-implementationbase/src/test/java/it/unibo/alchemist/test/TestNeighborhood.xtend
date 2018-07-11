/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
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
