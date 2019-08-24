package it.unibo.alchemist.model.implementations.conditions

import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node

/**
 * The condition is valid if the node contains the molecule.
 */
class ContainsMolecule<T>(
    node: Node<T>,
    private val molecule: Molecule
) : AbstractCondition<T>(node) {

    override fun getContext() = Context.LOCAL

    override fun getPropensityContribution() = if (isValid) 1.0 else 0.0

    override fun isValid() = node.contains(molecule)
}