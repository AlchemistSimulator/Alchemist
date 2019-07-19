package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of an heterogeneous pedestrian in the Euclidean world.
 */
class HeterogeneousPedestrian2D<T>(
    env: EuclideanPhysics2DEnvironment<T>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender
) : HeterogeneousPedestrianImpl<T>(env, rg, age, gender) {

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

    private val shape = env.shapeFactory.circle(1.0)

    /**
     * {@inheritDoc}
     */
    override fun getShape() = shape
}