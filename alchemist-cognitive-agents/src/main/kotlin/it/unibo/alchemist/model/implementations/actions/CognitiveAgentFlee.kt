package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Move the agent away from a target position. It's the opposite of [CognitiveAgentSeek].
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param coords
 *          the coordinates of the position the pedestrian moves away.
 */
open class CognitiveAgentFlee<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Node<T>,
    vararg coords: Double
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    private val danger: P = environment.makePosition(*coords.toTypedArray())

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentFlee<T, P, A> =
        CognitiveAgentFlee(environment, reaction, node, *danger.coordinates)

    override fun nextPosition(): P = (currentPosition - danger).resized(maxWalk)
}
