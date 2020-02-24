package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.surrounding
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * [Seek] behavior in a bidimensional environment. The actions performed are more
 * sophisticated and allow the pedestrian to try to avoid other agents on his/her path.
 */
open class Seek2D<T>(
    private val env: Environment<T, Euclidean2DPosition>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian<T>,
    vararg coords: Double
) : Seek<T, Euclidean2DPosition>(env, reaction, pedestrian, *coords) {

    override fun interpolatePositions(current: Euclidean2DPosition, target: Euclidean2DPosition, maxWalk: Double): Euclidean2DPosition {
        val superPosition = current + super.interpolatePositions(current, target, maxWalk)
        return (current.surrounding(env, maxWalk) + superPosition + currentPosition)
            .filter {
                if (env is EuclideanPhysics2DEnvironment) {
                    env.canNodeFitPosition(pedestrian, it)
                } else true
            }
            .minBy { it.getDistanceTo(super.target()) }?.minus(current)
            ?: currentPosition
    }
}
