/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.util

import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.MoleculeInput
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.MoleculeSurrogate
import it.unibo.alchemist.model.Molecule

/**
 * GraphQL compliant representation for a [Map.Entry] that contains a [Molecule] as a key
 * and its concentration as a value.
 *
 * @param molecule The molecule.
 * @param concentration The concentration represented as a Json String.
 */
data class GraphQLEntry(
    val molecule: MoleculeSurrogate,
    val concentration: String,
)

/**
 * Converts a [Map] of [Molecule]s to a list of [GraphQLEntry]s, making it possiible
 * to represent a [Map] of [Molecule]s as a GraphQL compliant object.
 *
 * @param T The type of the concentration.
 * @return a list of [GraphQLEntry]s representing the given [Map].
 */
fun <T> Map<Molecule, T>.toGraphQLEntryList(): List<GraphQLEntry> =
    map { GraphQLEntry(MoleculeSurrogate(it.key), encodeConcentrationContentToString(it.value ?: "")) }

/**
 * Utility function for retrieving a [GraphQLEntry] from a list of [GraphQLEntry]s by
 * its [Molecule]/[MoleculeSurrogate] name.
 *
 * @param name the name of the molecule
 * @return the [GraphQLEntry] with the associated [MoleculeSurrogate] and content, or null if not found.
 */
fun List<GraphQLEntry>.findByMoleculeName(name: String): GraphQLEntry? = find { it.molecule.name == name }

/**
 * Utility class for custom indexing of a list of [GraphQLEntry]s.
 */
object GraphQLListExtensions {
    /**
     * Utility function for retrieving the associated concentration from a list of [GraphQLEntry]s by
     * indexing it with a [MoleculeSurrogate].
     *
     * @param molecule the molecule to search for
     * @return the concentration associated to the given molecule, or null if not found.
     */
    operator fun List<GraphQLEntry>.get(molecule: MoleculeSurrogate) =
        this.find { it.molecule == molecule }?.concentration

    /**
     * Utility function for retrieving the associated concentration from a list of [GraphQLEntry]s by
     * indexing it with a [MoleculeInput].
     *
     * @param molecule the molecule to search for
     * @return the concentration associated to the given molecule, or null if not found.
     */
    operator fun List<GraphQLEntry>.get(molecule: MoleculeInput) =
        this.findByMoleculeName(molecule.name)?.concentration
}
