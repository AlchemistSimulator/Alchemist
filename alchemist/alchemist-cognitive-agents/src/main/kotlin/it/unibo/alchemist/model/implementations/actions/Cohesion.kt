package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.Vector

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
class Cohesion<T, P>(
    env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T>
) : AbstractGroupSteeringAction<T, P>(env, reaction, pedestrian)
    where
        P : Position<P>,
        P : Vector<P> {

    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        Cohesion(env, r, n as Pedestrian<T>)

    override fun nextPosition(): P = (centroid() - currentPosition).resizedToMaxWalkIfGreater()

    override fun group(): List<Pedestrian<T>> = pedestrian.membershipGroup.members
}
