package it.unibo.alchemist.model.implementations.movestrategies.speed

import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy

/**
 * Similar to [ConstantSpeed] but takes in consideration the time distribution's rate instead of the reaction's rate.
 */
class GloballyConstantSpeed<P : Position<P>>(
    private val reaction: Reaction<*>,
    private val maxSpeed: Double
) : SpeedSelectionStrategy<P> {
    override fun getNodeMovementLength(target: P) = maxSpeed / reaction.timeDistribution.rate
}