package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Blended
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution

/**
 * Reaction representing the steering behaviors of a pedestrian.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class SteeringBehavior<T, P : Position<P>>(
    private val env: Environment<T, P>,
    private val pedestrian: Pedestrian<T>,
    timeDistribution: TimeDistribution<T>
) : AbstractReaction<T>(pedestrian, timeDistribution) {

    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?) = TODO()

    override fun getRate() = timeDistribution.rate

    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) {}

    override fun execute() {
        with(actions.filterIsInstance<SteeringAction<T, P>>().toList()) {
            (actions - this).forEach { it.execute() }
            Blended(env, pedestrian, this).execute()
        }
    }
}