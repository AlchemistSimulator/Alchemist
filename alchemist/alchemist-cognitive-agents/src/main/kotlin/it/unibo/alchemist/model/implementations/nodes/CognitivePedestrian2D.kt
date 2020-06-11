package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

private typealias Group<T> = PedestrianGroup<T, Euclidean2DPosition, Euclidean2DTransformation>
private typealias CognitivePedestrianImplementation<T> = CognitivePedestrianImpl<T,
    Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>

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
class CognitivePedestrian2D<T> @JvmOverloads constructor(
    override val environment: Physics2DEnvironment<T>,
    randomGenerator: RandomGenerator,
    age: Age,
    gender: Gender,
    danger: Molecule? = null,
    group: Group<T>? = null
) : CognitivePedestrianImplementation<T>(
    environment,
    randomGenerator,
    age,
    gender,
    danger,
    group
), Pedestrian2D<T> {

    @JvmOverloads constructor(
        environment: Physics2DEnvironment<T>,
        randomGenerator: RandomGenerator,
        age: String,
        gender: String,
        danger: Molecule? = null,
        group: Group<T>? = null
    ) : this(environment, randomGenerator, Age.fromString(age), Gender.fromString(gender), danger, group)

    @JvmOverloads constructor(
        environment: Physics2DEnvironment<T>,
        randomGenerator: RandomGenerator,
        age: Int,
        gender: String,
        danger: Molecule? = null,
        group: Group<T>? = null
    ) : this(environment, randomGenerator, Age.fromYears(age), Gender.fromString(gender), danger, group)

    override val shape by lazy { super<Pedestrian2D>.shape }
    override val fieldOfView by lazy { super.fieldOfView }

    init {
        senses += fieldOfView
    }
}
