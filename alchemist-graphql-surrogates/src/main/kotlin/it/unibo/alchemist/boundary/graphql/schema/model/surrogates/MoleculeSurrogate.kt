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
import it.unibo.alchemist.model.Molecule

/**
 * A surrogate class for [Molecule].
 *
 * @param name the name of the molecule.
 */
@GraphQLDescription("A molecule with an associated name")
data class MoleculeSurrogate(
    @GraphQLIgnore override val origin: Molecule,
    val name: String = origin.name,
) : GraphQLSurrogate<Molecule>(origin)

/**
 * Converts a [Molecule] to a [MoleculeSurrogate].
 * @return a [MoleculeSurrogate] for this [Molecule]
 */
fun Molecule.toGraphQLMoleculeSurrogate() = MoleculeSurrogate(this)

/**
 * GraphQL input object for a molecule, that will avoid
 * the use of a full [Molecule] or [MoleculeSurrogate] when
 * client executes operations that require a molecule as parameter.
 *
 * @param name the name of the molecule.
 */
@GraphQLDescription("A molecule with an associated name, used as input object")
data class MoleculeInput(
    val name: String,
)
