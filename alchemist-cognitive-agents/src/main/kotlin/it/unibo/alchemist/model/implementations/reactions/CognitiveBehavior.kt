package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianCognitiveCapability

/**
 * Reaction representing the cognitive behavior of a pedestrian.
 *
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class CognitiveBehavior<T, V, A>(
    private val pedestrian: Node<T>,
    timeDistribution: TimeDistribution<T>
) : AbstractReaction<T>(pedestrian, timeDistribution)
    where V : Vector<V>, A : GeometricTransformation<V> {

    @Suppress("UNCHECKED_CAST")
    override fun cloneOnNewNode(node: Node<T>, currentTime: Time) =
        CognitiveBehavior(node, timeDistribution)

    override fun getRate() = timeDistribution.rate

    override fun updateInternalStatus(curTime: Time, executed: Boolean, env: Environment<T, *>) =
        pedestrian.asCapability<T, PedestrianCognitiveCapability<T>>().cognitiveModel.update(rate)
}
