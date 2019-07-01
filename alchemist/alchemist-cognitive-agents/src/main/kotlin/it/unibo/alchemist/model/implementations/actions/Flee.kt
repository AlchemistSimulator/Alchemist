package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D

open class Flee<T, P : Position2D<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    x: Double,
    y: Double
) : Seek<T, P>(env, pedestrian, x, y) {

    override fun getDestination(current: P, target: P, maxWalk: Double): P = super.getDestination(target, current, maxWalk)
}