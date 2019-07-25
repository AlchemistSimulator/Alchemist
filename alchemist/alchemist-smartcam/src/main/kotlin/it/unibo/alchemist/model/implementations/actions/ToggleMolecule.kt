package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction


/**
 * Action to toggle a molecule
 *
 * @param <T> concentration type
 * @param node the node containing the molecule
 * @param molecule the molecule to toggle
 * @param concentration the concentration to set or unset
 */
open class ToggleMolecule<T>(
    node: Node<T>,
    protected val molecule: Molecule,
    protected val concentration: T
) : AbstractAction<T>(node) {
    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        ToggleMolecule(n, molecule, concentration)

    /**
     * Toggles concentration.
     */
    override fun execute() {
        if (node.contains(molecule)) {
            node.removeConcentration(molecule)
        } else {
            node.setConcentration(molecule, concentration)
        }
    }

    override fun getContext() = Context.LOCAL
}