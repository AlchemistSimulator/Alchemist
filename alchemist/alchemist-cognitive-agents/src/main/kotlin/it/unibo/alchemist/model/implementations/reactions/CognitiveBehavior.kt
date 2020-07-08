package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Reaction representing the cognitive behavior of a pedestrian.
 *
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class CognitiveBehavior<T, V, A>(
    private val pedestrian: CognitivePedestrian<T, V, A>,
    timeDistribution: TimeDistribution<T>
) : AbstractReaction<T>(pedestrian, timeDistribution)
    where V : Vector<V>, A : GeometricTransformation<V> {

    @Suppress("UNCHECKED_CAST")
    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?) =
        CognitiveBehavior(n as CognitivePedestrian<T, V, A>, timeDistribution)

    override fun getRate() = timeDistribution.rate

    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) =
        pedestrian.cognitive.update(rate)
}
