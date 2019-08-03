package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.groups.Group
import it.unibo.alchemist.model.implementations.actions.utils.direction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic pedestrian in the Euclidean world.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 */
class HomogeneousPedestrian2D<T> @JvmOverloads constructor(
    env: EuclideanPhysics2DEnvironment<T>,
    rg: RandomGenerator,
    group: Group<T>? = null
) : HomogeneousPedestrianImpl<T, Euclidean2DPosition>(env, rg, group), Pedestrian2D<T> {

    init {
        env.setHeading(this, rg.direction())
        senses += sensorySpheres(env)
    }

    private val shape = shape(env)

    /**
     * {@inheritDoc}
     */
    override fun getShape() = shape
}