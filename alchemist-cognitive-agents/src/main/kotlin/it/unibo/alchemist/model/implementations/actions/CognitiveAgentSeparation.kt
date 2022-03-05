package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.properties.PerceptiveProperty

/**
 * Move the agent away from the pedestrians near to him.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 */
class CognitiveAgentSeparation<T>(
    val environment: Physics2DEnvironment<T>,
    reaction: Reaction<T>,
    override val pedestrian: Node<T>
) : AbstractGroupSteeringAction<T, Euclidean2DPosition, Euclidean2DTransformation>(environment, reaction, pedestrian) {

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentSeparation<T> =
        CognitiveAgentSeparation(environment, reaction, node)

    override fun nextPosition(): Euclidean2DPosition = (currentPosition - centroid()).coerceAtMost(maxWalk)

    override fun group(): List<Node<T>> = pedestrian.asProperty<T, PerceptiveProperty<T>>()
        .fieldOfView
        .influentialNodes()
        .filterIsInstance<Node<T>>()
        .plusElement(pedestrian)
}
