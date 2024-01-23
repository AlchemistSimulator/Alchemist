/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.GraphQLTestEnvironments
import it.unibo.alchemist.boundary.graphql.schema.model.NodeSurrogateTest.Companion.checkNodeSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NeighborhoodSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLNodeSurrogate
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.geometry.Vector

class EnvironmentSurrogateTest<T, P> : StringSpec({
    "EnvironmentSurrogate should map an Environment to a GraphQL compliant object" {
        GraphQLTestEnvironments.loadTests<T, P> {
            requireNotNull(it.simulation)
            val envSurrogate = it.toGraphQLEnvironmentSurrogate()
            it.dimensions shouldBe envSurrogate.dimensions
            it.nodes.size shouldBe envSurrogate.nodes().size
            it.nodes.forEach { node ->
                val nodeSurrogate = envSurrogate.nodeById(node.id)
                checkNodeSurrogate<T, P>(node, nodeSurrogate)
                checkPositionSurrogate(it.getPosition(node), envSurrogate.nodeToPos()[node.id]!!)
                checkNeighborhood(it.getNeighborhood(node), envSurrogate.getNeighborhood(node.id))
            }
            // Test propagation of changes
            val newNode = it.nodes.first().cloneNode(Time.ZERO)
            val newPosition = it.makePosition(0.0, 0.0)
            it.simulation.schedule {
                it.addNode(newNode, newPosition)
            }
            it.simulation.play()
            it.simulation.run()
            it.getNodeByID(newNode.id).toGraphQLNodeSurrogate() shouldBe envSurrogate.nodeById(newNode.id)
        }
    }
}) where T : Any, P : Position<P>, P : Vector<P> {
    companion object {
        private fun <T> checkNeighborhood(n: Neighborhood<T>, ns: NeighborhoodSurrogate<T>) {
            n.size() shouldBe ns.size
            n.isEmpty shouldBe ns.isEmpty()
            n.center.toGraphQLNodeSurrogate() shouldBe ns.getCenter()

            if (!n.isEmpty) {
                val node = n.neighbors.first()
                ns.contains(node.toGraphQLNodeSurrogate()) shouldBe true
                ns.getNeighbors().shouldContainAll(n.neighbors.map { it.toGraphQLNodeSurrogate() })
            }
        }
    }
}
