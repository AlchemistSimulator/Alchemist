package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.implementations.utils.surrounding
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * [Seek] behavior in a bidimensional environment. The actions performed are more
 * sophisticated and allow the pedestrian to try to avoid other agents on its path.
 * This behavior is restricted to two dimensions because some geometry utils available
 * only in 2D are required to implement it.
 */
open class Seek2D<T, P>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian<T>,
    vararg coords: Double
) : Seek<T, P>(env, reaction, pedestrian, *coords)
    where
        P : Position2D<P>,
        P : Vector2D<P> {

    public override fun interpolatePositions(current: P, target: P, maxWalk: Double): P {
        val superPosition = current + super.interpolatePositions(current, target, maxWalk)
        return (current.surrounding(env, maxWalk) + superPosition)
            .asSequence()
            .discardUnsuitablePositions(env, pedestrian)
            .minBy { it.distanceTo(target) }?.minus(current)
            ?: env.origin()
    }
}
