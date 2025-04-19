/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import it.unibo.alchemist.boundary.GraphQLTestEnvironments
import it.unibo.alchemist.boundary.graphql.schema.model.NodeSurrogateTest.Companion.checkNodeSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NeighborhoodSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLNodeSurrogate
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.geometry.Vector
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnvironmentSurrogateTest<T, P> where T : Any, P : Position<P>, P : Vector<P> {

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `EnvironmentSurrogate should map an Environment to a GraphQL compliant object`() {
        GraphQLTestEnvironments.loadTests<T, P> { envWrapper ->
            val simulation = requireNotNull(envWrapper.simulation, { "Simulation must not be null" })
            val envSurrogate = envWrapper.toGraphQLEnvironmentSurrogate()
            assertEquals(envWrapper.dimensions, envSurrogate.dimensions)
            assertEquals(envWrapper.nodes.size, envSurrogate.nodes().size)
            envWrapper.nodes.forEach { node ->
                val nodeSurrogate = envSurrogate.nodeById(node.id)
                checkNodeSurrogate<T, P>(node, nodeSurrogate)
                checkPositionSurrogate(envWrapper.getPosition(node), envSurrogate.nodeToPos()[node.id]!!)
                checkNeighborhood(envWrapper.getNeighborhood(node), envSurrogate.getNeighborhood(node.id))
            }
            // Test propagation of changes
            val newNode = envWrapper.nodes.first().cloneNode(Time.ZERO)
            val newPosition = envWrapper.makePosition(0.0, 0.0)
            simulation.schedule {
                envWrapper.addNode(newNode, newPosition)
            }
            simulation.play()
            simulation.run()
            assertEquals(
                newNode.toGraphQLNodeSurrogate(),
                envSurrogate.nodeById(newNode.id),
                "EnvironmentSurrogate should reflect newly added node",
            )
        }
    }

    companion object {
        fun <T> checkNeighborhood(neighborhood: Neighborhood<T>, neighborhoodSurrogate: NeighborhoodSurrogate<T>) {
            assertEquals(neighborhood.size(), neighborhoodSurrogate.size)
            assertEquals(neighborhood.isEmpty, neighborhoodSurrogate.isEmpty())
            assertEquals(neighborhood.center.toGraphQLNodeSurrogate(), neighborhoodSurrogate.getCenter())
            if (!neighborhood.isEmpty) {
                val node = neighborhood.neighbors.first()
                assertTrue(
                    neighborhoodSurrogate.contains(node.toGraphQLNodeSurrogate()),
                    "Neighborhood should contain node",
                )
                val expected = neighborhood.neighbors.map { it.toGraphQLNodeSurrogate() }
                val actual = neighborhoodSurrogate.getNeighbors()
                assertTrue(actual.containsAll(expected), "All neighbors should match in the surrogate")
            }
        }
    }
}
