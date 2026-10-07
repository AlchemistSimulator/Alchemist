/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction

/**
 * Valid when the concentration of [molecule] in the node equals [value].
 *
 * @param T concentration type
 */
class MoleculeHasConcentration<T>(reaction: NodeReaction<T>, private val molecule: Molecule, private val value: T) :
    AbstractLocalCondition<T>(reaction) {
    init {
        setValidity(
            targetNode.observeConcentration(molecule).map {
                it.fold({ false }) { current -> current == value }
            },
        )
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): MoleculeHasConcentration<T> =
        MoleculeHasConcentration(newReaction, molecule, value)

    override fun toString(): String = "$molecule=$value?[${isValid.current}]"
}
