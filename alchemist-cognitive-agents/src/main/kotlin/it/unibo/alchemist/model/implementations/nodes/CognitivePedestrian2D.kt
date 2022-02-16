package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.implementations.capabilities.BaseSpatial2DCapability
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.capabilities.SpatialCapability.Companion.defaultShapeRadius
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

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
open class CognitivePedestrian2D<T> constructor(
    randomGenerator: RandomGenerator,
    /*
     * This is final for a reason, see HomogeneousPedestrian2D.
     */
    final override val environment: Physics2DEnvironment<T>,
    backingNode: Node<T>,
    age: Age,
    gender: Gender,
    danger: Molecule? = null,
    group: PedestrianGroup2D<T>? = null
) :
    Pedestrian2D<T>,
    AbstractCognitivePedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>(
        environment, randomGenerator, backingNode, age, gender, danger, group
    ) {

    @JvmOverloads constructor(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        randomGenerator: RandomGenerator,
        environment: Physics2DEnvironment<T>,
        age: Any,
        gender: String,
        danger: Molecule? = null,
        group: PedestrianGroup2D<T>? = null,
        nodeCreationParameter: String? = null,
    ) : this(
        randomGenerator,
        environment,
        incarnation.createNode(randomGenerator, environment, nodeCreationParameter),
        Age.fromAny(age),
        Gender.fromString(gender),
        danger,
        group
    )

    init {
        backingNode.addCapability(
            BaseSpatial2DCapability(
                backingNode, environment.shapeFactory.circle(defaultShapeRadius)
            )
        )
    }
}
