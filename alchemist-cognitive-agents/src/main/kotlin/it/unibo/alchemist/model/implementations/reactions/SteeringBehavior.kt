package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.CognitiveAgentCombineSteering
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution

/**
 * Reaction representing the steering behavior of a pedestrian.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param node
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to which this reaction executes.
 * @param steerStrategy
 *          the strategy used to combine steering actions.
 */
open class SteeringBehavior<T>(
    private val environment: Environment<T, Euclidean2DPosition>,
    node: Node<T>,
    timeDistribution: TimeDistribution<T>,
    open val steerStrategy: SteeringStrategy<T, Euclidean2DPosition>
) : AbstractReaction<T>(node, timeDistribution) {

    /**
     * The list of steering actions in this reaction.
     */
    fun steerActions(): List<SteeringAction<T, Euclidean2DPosition>> =
        actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time) =
        SteeringBehavior(environment, node, timeDistribution, steerStrategy)

    override fun getRate() = timeDistribution.rate

    override fun updateInternalStatus(
        currentTime: Time?,
        hasBeenExecuted: Boolean,
        environment: Environment<T, *>?
    ) = Unit

    override fun execute() {
        (actions - steerActions()).forEach { it.execute() }
        CognitiveAgentCombineSteering(environment, this, node, steerActions(), steerStrategy).execute()
    }
}
