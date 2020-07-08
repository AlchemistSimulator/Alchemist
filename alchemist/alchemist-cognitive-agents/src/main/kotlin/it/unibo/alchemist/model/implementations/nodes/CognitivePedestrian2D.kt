package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

private typealias AbstractCognitivePedestrian2D<T> =
    AbstractCognitivePedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>

/**
 * Implementation of a cognitive pedestrian in the Euclidean world.
 *
 * @param environment
 *          the environment inside which this pedestrian moves.
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 * @param danger
 *          the molecule associated to danger in the environment.
 */
open class CognitivePedestrian2D<T> @JvmOverloads constructor(
    /*
     * This is final for a reason, see HomogeneousPedestrian2D.
     */
    final override val environment: Physics2DEnvironment<T>,
    randomGenerator: RandomGenerator,
    age: Age,
    gender: Gender,
    danger: Molecule? = null,
    group: PedestrianGroup2D<T>? = null
) : AbstractCognitivePedestrian2D<T>(
    environment,
    randomGenerator,
    age,
    gender,
    danger,
    group
), Pedestrian2D<T> {

    override val shape by lazy { super.shape }
    final override val fieldOfView by lazy { super.fieldOfView }

    init {
        senses += fieldOfView
    }

    @JvmOverloads constructor(
        environment: Physics2DEnvironment<T>,
        randomGenerator: RandomGenerator,
        age: String,
        gender: String,
        danger: Molecule? = null,
        group: PedestrianGroup2D<T>? = null
    ) : this(environment, randomGenerator, Age.fromString(age), Gender.fromString(gender), danger, group)

    @JvmOverloads constructor(
        environment: Physics2DEnvironment<T>,
        randomGenerator: RandomGenerator,
        age: Int,
        gender: String,
        danger: Molecule? = null,
        group: PedestrianGroup2D<T>? = null
    ) : this(environment, randomGenerator, Age.fromYears(age), Gender.fromString(gender), danger, group)
}
