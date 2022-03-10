package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.properties.SocialProperty

/**
 * Move the agent towards the other members of his group.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param node
 *          the owner of this action.
 */
class CognitiveAgentCohesion<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    node: Node<T>
) : AbstractGroupSteeringAction<T, P, A>(environment, reaction, node)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentCohesion<T, P, A> =
        CognitiveAgentCohesion(environment, reaction, node)

    override fun nextPosition(): P = (centroid() - currentPosition).coerceAtMost(maxWalk)

    override fun group() = node.asProperty<T, SocialProperty<T>>().group.members
}
