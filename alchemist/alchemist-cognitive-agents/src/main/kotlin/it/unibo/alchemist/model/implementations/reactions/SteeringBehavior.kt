package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Combine
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution

/**
 * Reaction representing the steering behavior of a pedestrian.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 * @param steerStrategy
 *          the logic according to the steering actions are combined.
 */
open class SteeringBehavior<T>(
    private val env: Environment<T, Euclidean2DPosition>,
    private val pedestrian: Pedestrian<T>,
    timeDistribution: TimeDistribution<T>,
    val steerStrategy: SteeringStrategy<T, Euclidean2DPosition>
) : AbstractReaction<T>(pedestrian, timeDistribution) {

    /**
     * The list of only the steering actions between all the actions in this reaction.
     */
    fun steerActions(): List<SteeringAction<T, Euclidean2DPosition>> =
        actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()

    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?) =
        SteeringBehavior(env, node as Pedestrian<T>, timeDistribution, steerStrategy)

    override fun getRate() = timeDistribution.rate

    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) = Unit

    override fun execute() {
        (actions - steerActions()).forEach { it.execute() }
        Combine(env, this, pedestrian, steerActions(), steerStrategy).execute()
    }
}
