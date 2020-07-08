package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

private typealias AbstractHeterogeneousPedestrian2D<T> =
    AbstractHeterogeneousPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>

/**
 * Implementation of a heterogeneous pedestrian in the Euclidean world.
 *
 * @param environment
 *          the environment inside which this pedestrian moves.
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 */
class HeterogeneousPedestrian2D<T> @JvmOverloads constructor(
    override val environment: Physics2DEnvironment<T>,
    randomGenerator: RandomGenerator,
    age: Age,
    gender: Gender,
    group: PedestrianGroup2D<T>? = null
) : AbstractHeterogeneousPedestrian2D<T>(
    environment,
    randomGenerator,
    age,
    gender,
    group
), Pedestrian2D<T> {

    override val shape by lazy { super.shape }
    override val fieldOfView by lazy { super.fieldOfView }

    init {
        senses += fieldOfView
    }

    @JvmOverloads constructor(
        env: Physics2DEnvironment<T>,
        rg: RandomGenerator,
        age: String,
        gender: String,
        group: PedestrianGroup2D<T>? = null
    ) : this(env, rg, Age.fromString(age), Gender.fromString(gender), group)

    @JvmOverloads constructor(
        env: Physics2DEnvironment<T>,
        rg: RandomGenerator,
        age: Int,
        gender: String,
        group: PedestrianGroup2D<T>? = null
    ) : this(env, rg, Age.fromYears(age), Gender.fromString(gender), group)
}
