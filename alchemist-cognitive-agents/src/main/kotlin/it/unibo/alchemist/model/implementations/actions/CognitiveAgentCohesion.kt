package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.properties.SocialProperty

/**
 * Move the agent towards the other members of his group.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 */
class CognitiveAgentCohesion<T, P, A>(
    env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Node<T>
) : AbstractGroupSteeringAction<T, P, A>(env, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    override fun cloneAction(n: Node<T>, r: Reaction<T>): CognitiveAgentCohesion<T, P, A> =
        CognitiveAgentCohesion(env, r, n)

    override fun nextPosition(): P = (centroid() - currentPosition).coerceAtMost(maxWalk)

    override fun group() = pedestrian.asCapability<T, SocialProperty<T>>().group.members
}
