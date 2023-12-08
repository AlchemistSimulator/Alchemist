/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model.surrogates

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.boundary.graphql.schema.util.toMoleculeToConcentrationMap
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node

/**
 * A GraphQL surrogate for a [Node] object.
 * @param T The type of the concentration.
 * @param id The id of the node.
 */
@GraphQLDescription("A node in the environment")
data class NodeSurrogate<T>(
    @GraphQLIgnore override val origin: Node<T>,
    val id: Int = origin.id,
) : GraphQLSurrogate<Node<T>>(origin) {

    /**
     * The number of [Molecule]s in this node.
     */
    val moleculeCount: Int
        get() = origin.moleculeCount

    /**
     * @return the list of properties/capabilities of this node as a string.
     */
    @GraphQLDescription("The list of properties/capabilities of this node as a string")
    fun properties() = origin.properties.map { it.toString() }

    /**
     * Returns all reactions of the node.
     */
    @GraphQLDescription("The list of reactions of this node")
    fun reactions() = origin.reactions.map { it.toGraphQLReactionSurrogate() }

    /**
     * @return the molecule corresponding to the i-th position.
     */
    @GraphQLDescription("List of Molecule-Concentration pairs")
    fun contents() = origin.contents.toMoleculeToConcentrationMap()

    /**
     * Calculates the concentration of a molecule.
     *
     * @param molecule the [MoleculeInput] object representing the molecule.
     */
    @GraphQLDescription("The concentration of the given molecule")
    fun getConcentration(molecule: MoleculeInput) = this.contents()[molecule]

    /**
     * Tests whether a node contains a [Molecule].
     *
     * @param molecule the [MoleculeInput] object representing the molecule.
     * @return true if the molecule is present, false otherwise
     */
    @GraphQLDescription("Whether the given molecule is present")
    fun contains(molecule: MoleculeInput): Boolean = this.contents()[molecule] != null
}

/**
 * Converts a [Node] to a [NodeSurrogate].
 * @return a [NodeSurrogate] for this [Node]
 */
fun <T> Node<T>.toGraphQLNodeSurrogate() = NodeSurrogate(this, id)
