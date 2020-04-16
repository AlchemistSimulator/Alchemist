package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.surrounding
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithObstacles

/**
 * [Seek] behavior in a bidimensional environment. The actions performed are more
 * sophisticated and allow the pedestrian to try to avoid other agents on its path.
 * This behavior is restricted to two dimensions because some geometry utils available
 * only in 2D are required to implement it.
 */
open class Seek2D<T>(
    private val env: Environment<T, Euclidean2DPosition>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian<T>,
    vararg coords: Double
) : Seek<T, Euclidean2DPosition>(env, reaction, pedestrian, *coords) {

    public override fun interpolatePositions(
        current: Euclidean2DPosition,
        target: Euclidean2DPosition,
        maxWalk: Double
    ): Euclidean2DPosition {
        val superPosition = current + super.interpolatePositions(current, target, maxWalk)
        return (current.surrounding(env, maxWalk) + superPosition)
            .map {
                if (env is EuclideanPhysics2DEnvironmentWithObstacles<*, T>) {
                    /*
                     * Take into account obstacles
                     */
                    env.next(current, it)
                } else it
            }
            .filter {
                if (env is Physics2DEnvironment) {
                    /*
                     * Take into account other pedestrians
                     */
                    env.canNodeFitPosition(pedestrian, it)
                } else true
            }
            .minBy { it.distanceTo(super.target()) }?.minus(current)
            ?: currentPosition
    }
}
