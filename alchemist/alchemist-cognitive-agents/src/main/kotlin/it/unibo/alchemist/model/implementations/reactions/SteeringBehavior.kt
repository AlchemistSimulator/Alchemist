package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Combine
import it.unibo.alchemist.model.interfaces.*

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
open class SteeringBehavior<T, P : Position<P>>(
    private val env: Environment<T, P>,
    private val pedestrian: Pedestrian<T>,
    timeDistribution: TimeDistribution<T>,
    private val steerStrategy: SteeringStrategy<T, P>
) : AbstractReaction<T>(pedestrian, timeDistribution) {

    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?) = TODO()

    override fun getRate() = timeDistribution.rate

    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) {}

    override fun execute() {
        with(actions.filterIsInstance<SteeringAction<T, P>>().toList()) {
            (actions - this).forEach { it.execute() }
            Combine(env, pedestrian, this, steerStrategy).execute()
        }
    }
}