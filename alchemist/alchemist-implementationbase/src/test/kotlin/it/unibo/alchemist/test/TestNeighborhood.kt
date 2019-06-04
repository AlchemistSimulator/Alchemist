/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.implementations.nodes.IntNode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests pertaining to the [it.unibo.alchemist.model.implementations.neighborhoods] package.
 */
class TestNeighborhood {
    /**
     * Tests whether the clone function of the
     * [it.unibo.alchemist.model.implementations.neighborhoods.SimpleNeighborhood] class works as expected.
     */
    @Test
    fun testClone() {
        val env = Continuous2DEnvironment<Int>()
        val n1 = IntNode(env)
        val n2 = IntNode(env)
        val neigh1 = Neighborhoods.make(env, n1, mutableListOf(n2))
        val neigh2 = neigh1.remove(n2)
        Assertions.assertEquals(0, neigh2.size())
        Assertions.assertTrue(neigh1.neighbors.contains(n2))
    }
}