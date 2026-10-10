/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractActionOnSingleMolecule
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule

/**
 * Changes the concentration of [molecule] in the cell by [deltaConcentration], which must not be zero.
 */
class ChangeBiomolConcentrationInCell(
    reaction: NodeReaction<Double>,
    molecule: Biomolecule,
    private val deltaConcentration: Double,
) : AbstractActionOnSingleMolecule<Double>(reaction, molecule) {
    init {
        require(deltaConcentration != 0.0) {
            "Changing the concentration of '$molecule' of 0 in node ${targetNode.id} makes no sense"
        }
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): ChangeBiomolConcentrationInCell =
        ChangeBiomolConcentrationInCell(newReaction, molecule as Biomolecule, deltaConcentration)

    override fun execute() =
        targetNode.setConcentration(molecule, targetNode.getConcentration(molecule) + deltaConcentration)

    override fun toString(): String =
        if (deltaConcentration >= 0) "${molecule.name}+$deltaConcentration" else "${molecule.name}$deltaConcentration"
}
