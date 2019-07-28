package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.actions.utils.direction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a cognitive pedestrian in the Euclidean world.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 * @param danger
 *          the molecule associated to danger in the environment.
 */
class CognitivePedestrian2D<T> @JvmOverloads constructor(
    env: EuclideanPhysics2DEnvironment<T>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender,
    danger: Molecule? = null
) : CognitivePedestrianImpl<T, Euclidean2DPosition>(env, rg, age, gender, danger), Pedestrian2D<T> {

    @JvmOverloads constructor(
        env: EuclideanPhysics2DEnvironment<T>,
        rg: RandomGenerator,
        age: String,
        gender: String,
        danger: Molecule? = null
    ) : this(env, rg, Age.fromString(age), Gender.fromString(gender), danger)

    @JvmOverloads constructor(
        env: EuclideanPhysics2DEnvironment<T>,
        rg: RandomGenerator,
        age: Int,
        gender: String,
        danger: Molecule? = null
    ) : this(env, rg, Age.fromYears(age), Gender.fromString(gender), danger)

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