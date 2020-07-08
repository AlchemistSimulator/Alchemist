package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

private typealias AbstractHomogeneousPedestrian2D<T> =
    AbstractHomogeneousPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>

/**
 * Implementation of a homogeneous pedestrian in the Euclidean world.
 *
 * @param environment
 *          the environment inside which this pedestrian moves.
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 */
class HomogeneousPedestrian2D<T> @JvmOverloads constructor(
    override val environment: Physics2DEnvironment<T>,
    randomGenerator: RandomGenerator,
    group: PedestrianGroup2D<T>? = null
) : AbstractHomogeneousPedestrian2D<T>(
    environment,
    randomGenerator,
    group
), Pedestrian2D<T> {

    override val shape by lazy { super.shape }
    override val fieldOfView by lazy { super.fieldOfView }

    init {
        senses += fieldOfView
    }
}
