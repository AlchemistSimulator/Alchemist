/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction

/**
 * A condition valid when the concentration of [molecule] in the node is lower than [requiredQuantity];
 * the reaction-specific rate law may use the distance from the threshold.
 *
 * @param T the concentration type
 */
class GenericMoleculeUnderLevel<T : Number>(reaction: NodeReaction<T>, molecule: Molecule, requiredQuantity: T) :
    GenericMoleculePresent<T>(reaction, molecule, requiredQuantity) {
    init {
        val threshold = requiredQuantity.toDouble()
        setValidity(
            targetNode.observeConcentration(molecule).map { concentration ->
                concentration.fold(ifEmpty = { Double.NEGATIVE_INFINITY }, ifSome = Number::toDouble) < threshold
            },
        )
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): GenericMoleculeUnderLevel<T> =
        GenericMoleculeUnderLevel(newReaction, getMolecule(), requiredQuantity)
}
