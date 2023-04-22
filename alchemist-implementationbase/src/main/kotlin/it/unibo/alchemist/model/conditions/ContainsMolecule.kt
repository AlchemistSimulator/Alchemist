/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node

/**
 * The condition is valid if the node contains the molecule.
 */
class ContainsMolecule<T>(
    node: Node<T>,
    private val molecule: Molecule,
) : AbstractNonPropensityContributingCondition<T>(node) {

    override fun getContext() = Context.LOCAL

    override fun isValid() = node.contains(molecule)
}
