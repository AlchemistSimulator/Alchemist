package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Combine
import it.unibo.alchemist.model.implementations.actions.Seek2D
import it.unibo.alchemist.model.implementations.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import kotlin.math.PI

/**
 * An [OrientingBehavior2D] with [SteeringAction]s as well. This class implements a
 * basic algorithm capable of mixing the steering behavior and the orienting behavior
 * smartly (e.g. avoiding situations in which the agent is blocked due to the voiding
 * of opposite forces). However, the resulting movements still present some shaking.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the environment.
 */
class OrientingSteeringBehavior2D<T, N : Euclidean2DConvexShape, E, M : ConvexPolygon> @JvmOverloads constructor(
    environment: Euclidean2DEnvironmentWithGraph<*, T, M, Euclidean2DPassage>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    timeDistribution: TimeDistribution<T>,
    private val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> = DistanceWeighted(environment, pedestrian)
) : OrientingBehavior2D<T, N, E, M>(environment, pedestrian, timeDistribution) {

    private var previous: Euclidean2DPosition? = null

    override fun moveTowards(target: Euclidean2DPosition, currentRoom: M?, targetDoor: Euclidean2DPassage) {
        val currPos = environment.getPosition(pedestrian)
        var desired = Seek2D(environment, this, pedestrian, *target.coordinates).nextPosition
        var disturbing = Combine(environment, this, pedestrian, steerActions(), steerStrategy).nextPosition
        /*
         * When the angle between the desired movement and the movement deriving from
         * other disturbing forces is > 90 degrees, we adjust the disturbing movement
         * so as to have an angle of 90 degrees.
         */
        if (desired.angleBetween(disturbing) > PI / 2) {
            disturbing = adjustDisturbingMovement(desired, disturbing)
        }
        /*
         * We also resize the desired movement to have a magnitude > of the disturbing movement
         * (empirically, resizing it so as to have a magnitude equal to the one of the disturbing
         * movement can still block the agent in some cases)
         */
        if (disturbing.magnitude > desired.magnitude) {
            desired = desired.resize(disturbing.magnitude * movementMagnitudeFactor)
        }
        var resulting = desired + disturbing
        previous?.let {
            resulting = it.times(alpha) + resulting.times(1 - alpha)
        }
        previous = resulting
        super.moveTowards(currPos + resulting, currentRoom, targetDoor)
    }

    private fun adjustDisturbingMovement(
        desiredMovement: Euclidean2DPosition,
        disturbingMovement: Euclidean2DPosition
    ): Euclidean2DPosition {
        val n = desiredMovement.normal()
        val length = disturbingMovement.magnitude
        val n1 = n.resize(length)
        val n2 = n.resize(-length)
        return if (n1.distanceTo(disturbingMovement) < n2.distanceTo(disturbingMovement)) {
            n1
        } else {
            n2
        }
    }

    private fun steerActions(): List<SteeringAction<T, Euclidean2DPosition>> =
        actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()

    companion object {
        /**
         * Emprically selected multiplicative factor for enlarging magnitude on disturbing movements.
         */
        const val movementMagnitudeFactor: Double = 1.2
        private const val alpha = 0.8
    }
}
