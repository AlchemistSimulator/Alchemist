package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment

/**
 * Move the agent away from the pedestrians near to him.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 */
class Separation<T>(
    override val env: Physics2DEnvironment<T>,
    reaction: Reaction<T>,
    override val pedestrian: Pedestrian2D<T>
) : AbstractGroupSteeringAction<T, Euclidean2DPosition>(env, reaction, pedestrian) {

    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        Separation(env, r, n as Pedestrian2D<T>)

    override fun nextPosition(): Euclidean2DPosition =
        (currentPosition - centroid()).resizeToMaxWalkIfGreater()

    override fun group(): List<Pedestrian<T>> = pedestrian.fieldOfView(env)
            .influentialNodes()
            .filterIsInstance<Pedestrian<T>>()
            .plusElement(pedestrian)
}
