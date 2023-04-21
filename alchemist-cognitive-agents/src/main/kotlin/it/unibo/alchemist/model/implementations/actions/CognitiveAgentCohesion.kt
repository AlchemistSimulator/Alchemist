package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty
import it.unibo.alchemist.model.interfaces.properties.SocialProperty

/**
 * Move the agent towards the other members of his group.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 */
class CognitiveAgentCohesion<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
) : AbstractGroupSteeringAction<T, P, A>(environment, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    private val socialGroup = node.asProperty<T, SocialProperty<T>>().group.members

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentCohesion<T, P, A> =
        CognitiveAgentCohesion(environment, reaction, node.pedestrianProperty)

    override fun nextPosition(): P = (centroid() - currentPosition).coerceAtMost(maxWalk)

    override fun group() = socialGroup
}
