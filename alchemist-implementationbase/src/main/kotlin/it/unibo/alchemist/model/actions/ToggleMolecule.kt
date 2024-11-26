/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction

/**
 * Treats [molecule] as a switch:
 * * if it is present, then it's removed from [node];
 * * otherwise, it is inserted in [node] with the provided [concentration].
 */
open class ToggleMolecule<T>(
    node: Node<T>,
    protected val molecule: Molecule,
    protected val concentration: T,
) : AbstractAction<T>(node) {
    override fun cloneAction(
        node: Node<T>,
        reaction: Reaction<T>,
    ) = ToggleMolecule(node, molecule, concentration)

    /**
     * Toggles concentration.
     */
    override fun execute() = if (isOn()) switchOff() else switchOn()

    /**
     * Returns true if it is on, already toggled.
     */
    protected fun isOn() = node.contains(molecule)

    /**
     * Switch off the molecule, or remove it.
     */
    private fun switchOff() = node.removeConcentration(molecule)

    /**
     * Switch on the molecule and set its concentration.
     */
    private fun switchOn() = node.setConcentration(molecule, concentration)

    override fun getContext() = Context.LOCAL
}
