package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.random.RandomGenerator

/**
 * Toggles a molecule randomly.
 *
 * @param <T> concentration type
 * @param node the node containing the molecule to toggle
 * @param rng random number generator to use
 * @param molecule the molecule to toggle
 * @param concentration the concentration to use for the "on" state
 * @param odds probability to toggle the molecule every time the action is triggered
 */
open class RandomlyToggleMolecule<T>(
    node: Node<T>,
    private val rng: RandomGenerator,
    molecule: Molecule,
    concentration: T,
    private val odds: Double
) : ToggleMolecule<T>(node, molecule, concentration) {

    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        RandomlyToggleMolecule(n, rng, molecule, concentration, odds)

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
    protected open fun shouldToggle() = rng.nextDouble() <= odds
}