package it.unibo.alchemist.model.implementations.conditions

import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction

/**
 * The condition is valid if all the other reactions having at least one condition can not execute.
 */
class Else<T>(
    node: Node<T>,
    private val myReaction: Reaction<T>
) : AbstractCondition<T>(node) {

    override fun getContext() = Context.LOCAL

    override fun getPropensityContribution() = if (isValid) 1.0 else 0.0

    override fun isValid() =
        node.reactions
            .asSequence()
            .filterNot { it == myReaction }
            .filter { it.conditions.isNotEmpty() }
            .map { it.canExecute() }
            .none()
}
