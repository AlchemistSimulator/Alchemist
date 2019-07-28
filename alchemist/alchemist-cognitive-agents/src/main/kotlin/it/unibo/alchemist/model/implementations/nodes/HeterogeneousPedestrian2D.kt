package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.actions.utils.direction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of an heterogeneous pedestrian in the Euclidean world.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 */
class HeterogeneousPedestrian2D<T>(
    env: EuclideanPhysics2DEnvironment<T>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender
) : HeterogeneousPedestrianImpl<T, Euclidean2DPosition>(env, rg, age, gender), Pedestrian2D<T> {

    constructor(
        env: EuclideanPhysics2DEnvironment<T>,
        rg: RandomGenerator,
        age: String,
        gender: String
    ) : this(env, rg, Age.fromString(age), Gender.fromString(gender))

    constructor(
        env: EuclideanPhysics2DEnvironment<T>,
        rg: RandomGenerator,
        age: Int,
        gender: String
    ) : this(env, rg, Age.fromYears(age), Gender.fromString(gender))

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