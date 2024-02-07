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
import io.kotest.matchers.shouldBe
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

class NodeSurrogateTest<T, P> : StringSpec({
    "NodeSurrogate should map a Node to a GraphQL compliant object" {
        GraphQLTestEnvironments.loadTests<T, P> {
            it.nodes.forEach { node ->
                checkNodeSurrogate<T, P>(node, node.toGraphQLNodeSurrogate())
            }
        }
    }
}) where T : Any, P : Position<P>, P : Vector<P> {
    companion object {
        fun <T : Any, P> checkNodeSurrogate(node: Node<T>, nodeSurrogate: NodeSurrogate<T>) {
            node.id shouldBe nodeSurrogate.id
            node.moleculeCount shouldBe nodeSurrogate.moleculeCount

            node.reactions.size shouldBe nodeSurrogate.reactions().size
            node.reactions.forEach { reaction ->
                checkReactionSurrogate(reaction, reaction.toGraphQLReactionSurrogate())
            }

            node.contents.forEach { (molecule, concentration) ->
                val concentrationSurrogate = requireNotNull(nodeSurrogate.contents()[MoleculeInput(molecule.name)])
                checkConcentrationContent(concentration, concentrationSurrogate)
            }

            node.contents.size shouldBe nodeSurrogate.contents().size
            node.contents.forEach { (molecule, concentration) ->
                val moleculeInput = MoleculeInput(molecule.name)
                nodeSurrogate.contains(moleculeInput) shouldBe true
                checkConcentrationContent(concentration, nodeSurrogate.getConcentration(moleculeInput)!!)
            }
        }

        fun <T>checkReactionSurrogate(reaction: Reaction<T>, reactionSurrogate: ReactionSurrogate<T>) {
            reaction.inputContext shouldBe reactionSurrogate.inputContext
            reaction.outputContext shouldBe reactionSurrogate.outputContext
            reaction.node.toGraphQLNodeSurrogate() shouldBe reactionSurrogate.node
        }
    }
}
