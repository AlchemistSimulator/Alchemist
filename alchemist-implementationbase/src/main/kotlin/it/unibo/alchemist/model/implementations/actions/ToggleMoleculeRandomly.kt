/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.random.RandomGenerator

/**
 * Treats [molecule] as a probabilistic switch:
 * * if it is present, then with probability [odds] it's removed from [node];
 * * otherwise, with probability [odds] it is inserted in [node] with the provided [concentration].
 *
 * @param <T> concentration type
 * @param node the node containing the molecule to toggle
 * @param randomGenerator random number generator to use
 * @param molecule the molecule to toggle
 * @param concentration the concentration to use for the "on" state
 * @param odds probability to toggle the molecule every time the action is triggered
 */
open class ToggleMoleculeRandomly<T>(
    node: Node<T>,
    private val randomGenerator: RandomGenerator,
    molecule: Molecule,
    concentration: T,
    private val odds: Double,
) : ToggleMolecule<T>(node, molecule, concentration) {

    init {
        require(odds in 0.0..1.0) {
            "Probability of toggling should in [0, 1], provided: $odds"
        }
    }

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        ToggleMoleculeRandomly(node, randomGenerator, molecule, concentration, odds)

    /**
     * Rolls the dice and toggles the molecule.
     */
    override fun execute() {
        if (shouldToggle()) {
            super.execute()
        }
    }

    /**
     * Returns true if it is time to toggle the molecule.
     */
    protected open fun shouldToggle() = randomGenerator.nextDouble() <= odds
}
