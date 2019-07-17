package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

/**
 * Move the agent away from a target position. It's the opposite of Seek.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param coords
 *          the coordinates of the position the pedestrian moves away.
 */
open class Flee<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    vararg coords: Double
) : Seek<T, P>(env, pedestrian, *coords) {

    override fun getDestination(current: P, target: P, maxWalk: Double): P = super.getDestination(target, current, maxWalk)
}