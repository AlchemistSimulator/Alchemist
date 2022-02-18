package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.capabilities.BasePerceptionOfOthers2D
import it.unibo.alchemist.model.implementations.capabilities.BaseSpatial2DCapability
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.capabilities.SpatialCapability.Companion.defaultShapeRadius
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
open class HomogeneousPedestrian2D<T> @JvmOverloads constructor(
    randomGenerator: RandomGenerator,
    /*
     * This is final because otherwise instancing a subclass overriding it would throw a NullPointerException when
     * computing fieldOfView (as such computation uses this property, which would not be initialised yet in the sub-
     * class, a bit tricky).
     */
    final override val environment: Physics2DEnvironment<T>,
    backingNode: Node<T>,
    group: Group<T>? = null
) : AbstractHomogeneousPedestrian2D<T>(
    randomGenerator,
    backingNode,
    group
),
    Pedestrian2D<T> {

    @JvmOverloads constructor(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        randomGenerator: RandomGenerator,
        environment: Physics2DEnvironment<T>,
        nodeCreationParameter: String? = null,
        group: Group<T>? = null
    ) : this(
        randomGenerator,
        environment,
        incarnation.createNode(randomGenerator, environment, nodeCreationParameter),
        group
    )

    init {
        backingNode.addCapability(BasePerceptionOfOthers2D(environment, backingNode))
        backingNode.addCapability(
            BaseSpatial2DCapability(
                backingNode,
                environment.shapeFactory.circle(defaultShapeRadius)
            )
        )
    }
}
