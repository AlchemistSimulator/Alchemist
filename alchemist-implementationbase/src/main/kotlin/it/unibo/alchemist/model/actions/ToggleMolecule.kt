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
 * Treats [molecule] as a switch:
 * * if it is present, then it's removed from [targetNode];
 * * otherwise, it is inserted in [targetNode] with the provided [concentration].
 */
open class ToggleMolecule<T>(
    reaction: NodeReaction<T>,
    protected val molecule: Molecule,
    protected val concentration: T,
) : AbstractLocalAction<T>(reaction) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): ToggleMolecule<T> =
        ToggleMolecule(newReaction, molecule, concentration)

    /**
     * Toggles concentration.
     */
    override fun execute() = if (isOn()) switchOff() else switchOn()

    /**
     * Returns true if it is on, already toggled.
     */
    protected fun isOn() = targetNode.contains(molecule)

    /**
     * Switch off the molecule, or remove it.
     */
    private fun switchOff() = targetNode.removeConcentration(molecule)

    /**
     * Switch on the molecule and set its concentration.
     */
    private fun switchOn() = targetNode.setConcentration(molecule, concentration)
}
