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
open class Flee<T, P, A>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    vararg coords: Double
) : AbstractSteeringAction<T, P, A>(env, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    private val danger: P = env.makePosition(*coords.toTypedArray())

    override fun cloneAction(n: Node<T>, r: Reaction<T>): Flee<T, P, A> =
        requireNodeTypeAndProduce<Pedestrian<T, P, A>, Flee<T, P, A>>(n) {
            Flee(env, r, it, *danger.coordinates)
        }

    override fun nextPosition(): P = (currentPosition - danger).resized(maxWalk)
}
