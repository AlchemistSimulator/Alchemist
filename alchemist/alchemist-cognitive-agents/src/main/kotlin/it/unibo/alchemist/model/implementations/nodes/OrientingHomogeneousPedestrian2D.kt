package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import org.apache.commons.math3.random.RandomGenerator

/**
 * A homogeneous [OrientingPedestrian] in an [EuclideanPhysics2DEnvironmentWithGraph].
 *
 * @param T the concentration type.
 * @param M the type of nodes of the navigation graph provided by the environment.
 * @param F the type of edges of the navigation graph provided by the environment.
 */
class OrientingHomogeneousPedestrian2D<T, M : ConvexPolygon, F : GraphEdge<M>> @JvmOverloads constructor(
    knowledgeDegree: Double,
    randomGenerator: RandomGenerator,
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, F>,
    group: PedestrianGroup<T>? = null
) : OrientingPedestrian2D<T, M, F>(knowledgeDegree, randomGenerator, environment, group) {

    private val shape = shape(environment)

    init {
        senses += fieldOfView(environment)
    }

    /**
     * @inheritDoc
     */
    override fun getShape() = shape
}
