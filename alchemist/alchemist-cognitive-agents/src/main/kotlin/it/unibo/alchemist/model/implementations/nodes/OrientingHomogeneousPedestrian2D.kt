package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator

/**
 * An orienting homogeneous pedestrian in a [EuclideanPhysics2DEnvironment].
 *
 * @param N1 the type of nodes in the [envGraph].
 * @param E1 the type of edges of the [envGraph].
 * @param T  the concentration type.
 */
class OrientingHomogeneousPedestrian2D<N1 : ConvexPolygon, E1 : GraphEdge<N1>, T> @JvmOverloads constructor(
    knowledgeDegree: Double,
    rg: RandomGenerator,
    envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, N1, E1>,
    env: EuclideanPhysics2DEnvironment<T>,
    group: PedestrianGroup<T>? = null
) : AbstractOrientingPedestrian2D<N1, E1, T>(knowledgeDegree, rg, envGraph, env, group) {

    private val shape = shape(env)

    init {
        senses += fieldOfView(env)
    }

    /**
     */
    override fun getShape() = shape
}
