/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests pertaining to the [it.unibo.alchemist.model.implementations.neighborhoods] package.
 */
class TestNeighborhood {
    private fun createIntNode(
        incarnation: Incarnation<Int, Euclidean2DPosition>,
        environment: Environment<Int, Euclidean2DPosition>
    ): Node<Int> {
        return object : GenericNode<Int>(incarnation, environment) {
            override fun createT(): Int = 0
        }
    }
    /**
     * Tests whether the clone function of the
     * [it.unibo.alchemist.model.implementations.neighborhoods.SimpleNeighborhood] class works as expected.
     */
    @Test
    fun testClone() {
        val incarnation = SupportedIncarnations.get<Int, Euclidean2DPosition>("protelis").orElseThrow()
        val environment = Continuous2DEnvironment<Int>(incarnation)
        val n1 = createIntNode(incarnation, environment)
        val n2 = createIntNode(incarnation, environment)
        val neigh1 = Neighborhoods.make(environment, n1, mutableListOf(n2))
        val neigh2 = neigh1.remove(n2)
        Assertions.assertEquals(0, neigh2.size())
        Assertions.assertTrue(neigh1.neighbors.contains(n2))
    }
}
