/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.util

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.MoleculeInput
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.MoleculeSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLMoleculeSurrogate
import it.unibo.alchemist.model.Molecule

/**
 * A GraphQL compliant representation of a [Map] storing [Molecule]s and their concentrations.
 */
@GraphQLDescription("A map storing molecules and their concentrations")
data class MoleculeToConcentrationMap(
    @GraphQLIgnore override val originMap: Map<MoleculeSurrogate, String>,
    override val size: Int = originMap.size,
) : GraphQLMap<MoleculeSurrogate, String>(originMap, size) {
    /**
     * @return the list of entries in this map.
     */
    @GraphQLDescription("The list of pairs Molecule-Concentration")
    fun entries(): List<MoleculeToConcentrationEntry> =
        originMap.map { (molecule, concentration) ->
            MoleculeToConcentrationEntry(molecule, concentration)
        }

    /**
     * Custom indexing with a [MoleculeInput] object.
     * NB: key resolution is done by the wrapped [Molecule] name.
     */
    @GraphQLIgnore operator fun get(input: MoleculeInput) =
        originMap.filterKeys { it.name == input.name }.values.firstOrNull()
}

/**
 * A single entry in a [MoleculeToConcentrationMap], storing a [MoleculeSurrogate] and its concentration.
 *
 * @param molecule the molecule
 * @param concentration the concentration of the molecule
 */
@GraphQLDescription("A pair Molecule-Concentration")
data class MoleculeToConcentrationEntry(
    val molecule: MoleculeSurrogate,
    val concentration: String,
)

/**
 * Converts a [Map] of [Molecule]s and concentration of type [T] to a [MoleculeToConcentrationMap].
 */
fun <T>Map<Molecule, T>.toMoleculeToConcentrationMap() =
    MoleculeToConcentrationMap(
        this.mapKeys { it.key.toGraphQLMoleculeSurrogate() }
            .mapValues { encodeConcentrationContentToString(it.value) },
    )
