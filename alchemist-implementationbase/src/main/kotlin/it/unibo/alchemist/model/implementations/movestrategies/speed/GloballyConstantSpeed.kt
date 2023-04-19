package it.unibo.alchemist.model.implementations.movestrategies.speed

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy

/**
 * Similar to [ConstantSpeed] but takes in consideration the time distribution's rate instead of the reaction's rate.
 */
class GloballyConstantSpeed<T, P : Position<P>>(
    private val reaction: Reaction<*>,
    private val maxSpeed: Double,
) : SpeedSelectionStrategy<T, P> {
    override fun getNodeMovementLength(target: P) = maxSpeed / reaction.timeDistribution.rate

    override fun cloneIfNeeded(destination: Node<T>, reaction: Reaction<T>): GloballyConstantSpeed<T, P> =
        GloballyConstantSpeed(reaction, maxSpeed)
}
