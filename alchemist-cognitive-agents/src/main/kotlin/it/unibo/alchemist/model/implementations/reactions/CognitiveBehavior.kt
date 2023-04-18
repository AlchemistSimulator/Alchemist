package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty

/**
 * Reaction representing the cognitive behavior of a pedestrian.
 *
 * @param node
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class CognitiveBehavior<T, V, A>(
    node: Node<T>,
    timeDistribution: TimeDistribution<T>,
) : AbstractReaction<T>(node, timeDistribution)
    where V : Vector<V>, A : GeometricTransformation<V> {

    @Suppress("UNCHECKED_CAST")
    override fun cloneOnNewNode(node: Node<T>, currentTime: Time) =
        CognitiveBehavior(node, timeDistribution)

    override fun updateInternalStatus(curTime: Time, executed: Boolean, environment: Environment<T, *>) =
        node.asProperty<T, CognitiveProperty<T>>().cognitiveModel.update(rate)
}
