/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction

/**
 * Sets the concentration of [molecule] in the node owning this action to [value].
 *
 * @param T concentration type
 */
class SetLocalMoleculeConcentration<T>(reaction: NodeReaction<T>, molecule: Molecule, private val value: T) :
    AbstractActionOnSingleMolecule<T>(reaction, molecule) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): SetLocalMoleculeConcentration<T> =
        SetLocalMoleculeConcentration(newReaction, molecule, value)

    override fun execute() = targetNode.setConcentration(molecule, value)
}
