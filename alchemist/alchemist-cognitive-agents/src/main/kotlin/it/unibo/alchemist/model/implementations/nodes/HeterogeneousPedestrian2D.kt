package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup
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
class HeterogeneousPedestrian2D<T> @JvmOverloads constructor(
    env: EuclideanPhysics2DEnvironment<T>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender,
    group: PedestrianGroup<T>? = null
) : HeterogeneousPedestrianImpl<T, Euclidean2DPosition>(env, rg, age, gender, group), Pedestrian2D<T> {

    @JvmOverloads constructor(
        env: EuclideanPhysics2DEnvironment<T>,
        rg: RandomGenerator,
        age: String,
        gender: String,
        group: PedestrianGroup<T>? = null
    ) : this(env, rg, Age.fromString(age), Gender.fromString(gender), group)

    @JvmOverloads constructor(
        env: EuclideanPhysics2DEnvironment<T>,
        rg: RandomGenerator,
        age: Int,
        gender: String,
        group: PedestrianGroup<T>? = null
    ) : this(env, rg, Age.fromYears(age), Gender.fromString(gender), group)

    private val shape = shape(env)

    init {
        senses += fieldOfView(env)
    }

    /**
     * {@inheritDoc}
     */
    override fun getShape() = shape
}
