package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.influencesphere.FieldOfView2D
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a cognitive pedestrian in the Euclidean world.
 */
class CognitivePedestrian2D<T> @JvmOverloads constructor(
    env: EuclideanPhysics2DEnvironment<T>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender,
    danger: Molecule? = null
) : CognitivePedestrianImpl<T, Euclidean2DPosition>(env, rg, age, gender, danger), Pedestrian2D {

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
        sensory.add(FieldOfView2D(env, this))
    }

    private val shape = env.defaultShape()

    /**
     * {@inheritDoc}
     */
    override fun getShape() = shape
}