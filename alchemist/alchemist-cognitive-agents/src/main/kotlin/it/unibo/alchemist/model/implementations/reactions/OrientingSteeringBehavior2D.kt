package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Combine
import it.unibo.alchemist.model.implementations.actions.Seek
import it.unibo.alchemist.model.implementations.actions.Seek2D
import it.unibo.alchemist.model.implementations.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.implementations.geometry.*
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexEuclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import kotlin.math.PI

/**
 * An [OrientingBehavior2D] with [SteeringAction]s as well. This class implements a
 * basic algorithm capable of mixing the steering behavior and the orienting behavior
 * smartly (e.g. avoiding situations in which the agent is blocked due to the voiding
 * of opposite forces). However, the resulting movements still present some shaking.
 *
 * @param N1 the type of nodes of the [envGraph].
 * @param E1 the type of edges of the [envGraph].
 * @param N2 the type of landmarks of the pedestrian's cognitive map.
 * @param E2 the type of edges of the pedestrian's cognitive map.
 * @param T  the concentration type.
 */
class OrientingSteeringBehavior2D<N1 : ConvexPolygon, E1 : GraphEdgeWithData<N1, Euclidean2DSegment>, N2: ConvexEuclidean2DShape, E2 : GraphEdge<N2>, T> @JvmOverloads constructor(
    private val env: Environment<T, Euclidean2DPosition>,
    private val pedestrian: OrientingPedestrian<Euclidean2DPosition, Euclidean2DTransformation, N2, E2, T>,
    timeDistribution: TimeDistribution<T>,
    private val envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, N1, E1>,
    private val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> = DistanceWeighted(env, pedestrian)
) : OrientingBehavior2D<N1, E1, N2, E2, T>(env, pedestrian, timeDistribution, envGraph) {

    override fun moveTowards(target: Euclidean2DPosition, currentRoom: N1?) {
        val currPos = env.getPosition(pedestrian)
        var desiredMovement = Seek2D(env, this, pedestrian, *target.cartesianCoordinates).nextPosition
        var disturbingMovement = Combine(env, this, pedestrian, steerActions(), steerStrategy).nextPosition
        /*
         * When the angle between the desired movement and the movement deriving from
         * other disturbing forces is > 90 degrees, we need to adjust the latter movement
         * in order to prevent such forces to cause an unrealistic situation in which the
         * pedestrian remains blocked or goes in the opposite direction with respect to the
         * target position.
         */
        if (desiredMovement.angleBetween(disturbingMovement) > PI / 2) {
            disturbingMovement = adjustDisturbingMovement(desiredMovement, disturbingMovement)
        }
        /*
         * We also resize the desired movement to have a magnitude > of the disturbing movement
         * (empirically, resizing it so as to have a magnitude equal to the one of the disturbing
         * movement can still block the agent in some cases)
         */
        if (disturbingMovement.magnitude() > desiredMovement.magnitude()) {
            desiredMovement = desiredMovement.resize(disturbingMovement.magnitude() * 1.2)
        }
        val movement = desiredMovement + disturbingMovement
        val nextPosition = currPos + movement
        super.moveTowards(nextPosition, currentRoom)
    }

    private fun adjustDisturbingMovement(desiredMovement: Euclidean2DPosition, disturbingMovement: Euclidean2DPosition): Euclidean2DPosition {
        val n = desiredMovement.normal()
        val length = disturbingMovement.magnitude()
        val n1 = n.resize(length)
        val n2 = n.resize(-length)
        return if (n1.getDistanceTo(disturbingMovement) < n2.getDistanceTo(disturbingMovement)) {
            n1
        } else {
            n2
        }
    }

    private fun steerActions(): List<SteeringAction<T, Euclidean2DPosition>> =
        actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()
}
