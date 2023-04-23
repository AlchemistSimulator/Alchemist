package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty
import it.unibo.alchemist.model.reactions.AbstractReaction

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

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time) =
        CognitiveBehavior(node, timeDistribution)

    override fun updateInternalStatus(curTime: Time, executed: Boolean, environment: Environment<T, *>) =
        node.asProperty<T, CognitiveProperty<T>>().cognitiveModel.update(rate)
}
