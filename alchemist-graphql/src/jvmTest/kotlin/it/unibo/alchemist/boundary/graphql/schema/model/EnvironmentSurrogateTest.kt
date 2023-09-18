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
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.boundary.TestingEnvironments.graphqlTestEnvironmnets
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.TimeSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLNodeSurrogate
import it.unibo.alchemist.boundary.graphql.schema.util.PositionSurrogateUtils
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.geometry.Vector

class EnvironmentSurrogateTest<T, P> : StringSpec({
    "EnvironmentSurrogate should map an Environment to a GraphQL compliant object" {
        graphqlTestEnvironmnets<T, P>().forEach {
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
            it.addNode(newNode, newPosition)
            it.getNodeByID(newNode.id).toGraphQLNodeSurrogate() shouldBe envSurrogate.nodeById(newNode.id)

            // Test node cloning
            val newPosition1 = it.makePosition(1.0, 1.0)
            val cloenedNode = envSurrogate.cloneNode(
                it.nodes.first().id,
                PositionSurrogateUtils.toPositionSurrogate(newPosition1).toInputPosition(),
                TimeSurrogate.ZERO,
            ) shouldNotBe null

            envSurrogate.nodeToPos()[cloenedNode!!.id] shouldBe PositionSurrogateUtils.toPositionSurrogate(newPosition1)

            envSurrogate.nodes().shouldContainAll(it.nodes.map { node -> node.toGraphQLNodeSurrogate() })
        }
    }
}) where T : Any, P : Position<P>, P : Vector<P>
