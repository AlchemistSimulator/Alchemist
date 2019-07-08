package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Blended
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution

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
            if (size > 1) Blended(env, pedestrian, this).execute() else this.first().execute()
        }
    }
}