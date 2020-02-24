package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.Combine
import it.unibo.alchemist.model.implementations.actions.FollowFlowField
import it.unibo.alchemist.model.implementations.actions.Seek
import it.unibo.alchemist.model.implementations.actions.SeekCloser
import it.unibo.alchemist.model.implementations.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.implementations.geometry.*
import it.unibo.alchemist.model.implementations.geometry.graph.Euclidean2DCrossing
import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdgeWithData
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexEuclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import kotlin.math.PI

/**
 */
class OrientingSteeringBehavior2D<T> @JvmOverloads constructor(
    private val env: EuclideanPhysics2DEnvironment<T>,//Environment<T, Euclidean2DPosition>,
    private val pedestrian: OrientingPedestrian<Euclidean2DPosition, Euclidean2DTransformation, ConvexEuclidean2DShape, out GraphEdge<ConvexEuclidean2DShape>, T>,
    timeDistribution: TimeDistribution<T>,
    private val envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>,
    private val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> = DistanceWeighted(env, pedestrian)
) : AbstractOrientingBehavior<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing, ConvexEuclidean2DShape, T>(env, pedestrian, timeDistribution, envGraph) {

    private val orientingBehavior2D = OrientingBehavior2D(env, pedestrian, timeDistribution, envGraph)

    override fun computeEdgeRankings(currRoom: ConvexPolygon, destination: Euclidean2DPosition) =
        orientingBehavior2D.computeEdgeRankings(currRoom, destination)

    override fun computeSubdestination(targetEdge: GraphEdgeWithData<ConvexPolygon, Euclidean2DSegment>) =
        orientingBehavior2D.computeSubdestination(targetEdge)

    override fun weight(e: Euclidean2DCrossing, rank: Int?): Double =
        orientingBehavior2D.weight(e, rank)

    override fun moveTowards(currRoom: ConvexPolygon, target: Euclidean2DPosition, currPosition: Euclidean2DPosition) {
        /*
        if (!currRoom.contains(currPosition)) {
            return super.moveTowards(currRoom, target, currPosition)
        }
         */
        /*
         * Vector representing the movement to the desired position, i.e. the
         * farthest position the pedestrian could reach in this execution step towards
         * the target depending on his/her physical characteristics.
         */
        var desiredMovement = SeekCloser(env, this, pedestrian, *target.cartesianCoordinates).nextPosition
        /*
         * Vector representing the movement of the pedestrian considering only the combination
         * of the other social forces a part from the will to reach the target position.
         */
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
        if (disturbingMovement.magnitude() > desiredMovement.magnitude()) {
            desiredMovement = desiredMovement.resize(disturbingMovement.magnitude() * 1.2)
        }
        /*
         * The movement the pedestrian will make.
         */
        val movement = desiredMovement + disturbingMovement
        var nextPosition = currPosition + movement
        /*
         * If such movement leads the pedestrian outside the current room (without crossing
         * the target edge, which would be fine), we need to adjust it.
         */
        if (!currRoom.contains(nextPosition)
                && (state == State.MOVING_TO_FINAL || !crosses(currPosition, movement, targetEdge!!))) {
            nextPosition = adjustFinalMovement(currPosition, nextPosition, currRoom)
        }
        SeekCloser(env, this, pedestrian, *nextPosition.cartesianCoordinates).execute()
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

    private fun crosses(currPosition: Euclidean2DPosition, movement: Euclidean2DPosition, targetEdge: GraphEdgeWithData<ConvexPolygon, Euclidean2DSegment>): Boolean =
        intersection(Pair(currPosition, currPosition + movement), targetEdge.data).type ==
            SegmentsIntersectionTypes.POINT

    private fun adjustFinalMovement(currPosition: Euclidean2DPosition, nextPosition: Euclidean2DPosition, currRoom: ConvexPolygon): Euclidean2DPosition =
        currRoom.vertices().indices
            .map { currRoom.getEdge(it) }
            .filter {
                intersection(Pair(currPosition, nextPosition), it).type == SegmentsIntersectionTypes.POINT
            }
            .map {
                intersection(it, currPosition, nextPosition.getDistanceTo(currPosition))
            }
            .flatMap { mutableListOf(it.p1, it.p2) }
            .filter { it.isPresent }
            .map { it.get() }
            .filter { (it - currPosition).angleBetween(nextPosition - currPosition) <= PI / 2 }
            .minBy { it.getDistanceTo(nextPosition) } ?: nextPosition

    /**
     */
    @Suppress("UNCHECKED_CAST")
    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?): Reaction<T> {
        try {
            n as OrientingPedestrian<Euclidean2DPosition, Euclidean2DTransformation, ConvexEuclidean2DShape, out GraphEdge<ConvexEuclidean2DShape>, T>
            return OrientingSteeringBehavior2D(env, n, timeDistribution, envGraph, steerStrategy)
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("node not compatible")
        }
    }

    private fun steerActions(): List<SteeringAction<T, Euclidean2DPosition>> =
        actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()
}
