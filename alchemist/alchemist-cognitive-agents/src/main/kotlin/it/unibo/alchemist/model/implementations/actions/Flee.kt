package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Move the agent away from a target position. It's the opposite of Seek.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param coords
 *          the coordinates of the position the pedestrian moves away.
 */
open class Flee<T, P, A : GeometricTransformation<P>>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    vararg coords: Double
) : AbstractSteeringAction<T, P, A>(env, reaction, pedestrian)
    where
        P : Position<P>,
        P : Vector<P> {

    private val danger: P = env.makePosition(*coords.toTypedArray())

    @Suppress("UNCHECKED_CAST")
    override fun cloneAction(n: Node<T>, r: Reaction<T>): Flee<T, P, A> =
        Flee(env, r, n as Pedestrian<T, P, A>, *danger.coordinates)

    /*
     * We don't want to resizeToMaxWalkIfGreater, we want to flee from danger as
     * much as we can (i.e. maxWalk).
     */
    override fun nextPosition(): P = (currentPosition - danger).resized(maxWalk())
}
