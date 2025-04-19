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
import it.unibo.alchemist.boundary.graphql.schema.model.ConcentrationSurrogateTest.Companion.checkConcentrationContent
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.MoleculeInput
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NodeSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.ReactionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLNodeSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLReactionSurrogate
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.geometry.Vector
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NodeSurrogateTest<T, P> where T : Any, P : Position<P>, P : Vector<P> {

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `NodeSurrogate should map a Node to a GraphQL compliant object`() {
        GraphQLTestEnvironments.loadTests<T, P> {
            it.nodes.forEach { node ->
                checkNodeSurrogate(node, node.toGraphQLNodeSurrogate())
            }
        }
    }

    companion object {
        fun <T : Any> checkNodeSurrogate(node: Node<T>, nodeSurrogate: NodeSurrogate<T>) {
            assertEquals(node.id, nodeSurrogate.id, "Node ID mismatch")
            assertEquals(node.moleculeCount, nodeSurrogate.moleculeCount, "Molecule count mismatch")
            assertEquals(node.reactions.size, nodeSurrogate.reactions().size, "Reaction count mismatch")
            node.reactions.forEach { reaction ->
                checkReactionSurrogate(reaction, reaction.toGraphQLReactionSurrogate())
            }
            node.contents.forEach { (molecule, concentration) ->
                val surrogate = nodeSurrogate.contents()[MoleculeInput(molecule.name)]
                assertNotNull(surrogate, "Surrogate concentration should not be null for molecule ${molecule.name}")
                checkConcentrationContent(concentration, surrogate)
            }
            assertEquals(node.contents.size, nodeSurrogate.contents().size, "Content map size mismatch")
            node.contents.forEach { (molecule, concentration) ->
                val moleculeInput = MoleculeInput(molecule.name)
                assertTrue(nodeSurrogate.contains(moleculeInput), "NodeSurrogate should contain $moleculeInput")
                val actual = nodeSurrogate.getConcentration(moleculeInput)
                assertNotNull(actual, "Concentration should not be null for $moleculeInput")
                checkConcentrationContent(concentration, actual)
            }
        }
        fun <T> checkReactionSurrogate(reaction: Reaction<T>, reactionSurrogate: ReactionSurrogate<T>) {
            assertEquals(reaction.inputContext, reactionSurrogate.inputContext, "Input context mismatch")
            assertEquals(reaction.outputContext, reactionSurrogate.outputContext, "Output context mismatch")
            assertEquals(reaction.node.toGraphQLNodeSurrogate(), reactionSurrogate.node, "Node mapping mismatch")
        }
    }
}
