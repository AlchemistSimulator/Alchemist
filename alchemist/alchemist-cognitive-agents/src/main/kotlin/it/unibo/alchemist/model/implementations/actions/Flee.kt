package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

/**
 * Moves the agent away from a target position. It's the opposite of Seek.
 */
open class Flee<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    vararg coords: Double
) : Seek<T, P>(env, pedestrian, *coords) {

    override fun getDestination(current: P, target: P, maxWalk: Double): P = super.getDestination(target, current, maxWalk)
}