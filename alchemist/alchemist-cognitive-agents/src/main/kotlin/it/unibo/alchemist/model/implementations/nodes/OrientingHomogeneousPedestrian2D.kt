package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator

/**
 * A homogeneous [OrientingPedestrian2D] in a [EuclideanPhysics2DEnvironment].
 *
 * @param T the concentration type.
 * @param M the type of nodes of the [environmentGraph].
 * @param F the type of edges of the [environmentGraph].
 */
class OrientingHomogeneousPedestrian2D<T, M : ConvexPolygon, F : GraphEdge<M>> @JvmOverloads constructor(
    knowledgeDegree: Double,
    randomGenerator: RandomGenerator,
    environmentGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, M, F>,
    environment: EuclideanPhysics2DEnvironment<T>,
    group: PedestrianGroup<T>? = null
) : OrientingPedestrian2D<T, M, F>(knowledgeDegree, randomGenerator, environmentGraph, environment, group) {

    private val shape = shape(environment)

    init {
        senses += fieldOfView(environment)
    }

    /**
     * @inheritDoc
     */
    override fun getShape() = shape
}
