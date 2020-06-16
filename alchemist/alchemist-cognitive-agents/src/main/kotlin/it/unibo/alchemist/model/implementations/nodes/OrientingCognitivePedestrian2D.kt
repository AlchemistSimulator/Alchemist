package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.OrientingCognitivePedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.graph.DefaultEdge

private typealias PGroup<T> = PedestrianGroup<T, Euclidean2DPosition, Euclidean2DTransformation>
/**
 * A cognitive [OrientingPedestrian] in an [EuclideanPhysics2DEnvironmentWithGraph].
 *
 * @param T the concentration type.
 * @param M the type of nodes of the navigation graph provided by the environment.
 * @param F the type of edges of the navigation graph provided by the environment.
 */
class OrientingCognitivePedestrian2D<T, M : ConvexPolygon, F> @JvmOverloads constructor(
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, F>,
    randomGenerator: RandomGenerator,
    knowledgeDegree: Double,
    group: PGroup<T>? = null,
    override val age: Age,
    override val gender: Gender,
    danger: Molecule? = null
) : OrientingPedestrian2D<T, M, F>(environment, randomGenerator, knowledgeDegree = knowledgeDegree, group = group),
    OrientingCognitivePedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Ellipse, DefaultEdge> {

    /**
     * Allows to specify age and gender with a string.
     */
    @JvmOverloads constructor(
        knowledgeDegree: Double,
        randomGenerator: RandomGenerator,
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, F>,
        group: PGroup<T>? = null,
        age: String,
        gender: String,
        danger: Molecule? = null
    ) : this(
        environment,
        randomGenerator,
        knowledgeDegree,
        group,
        Age.fromString(age),
        Gender.fromString(gender),
        danger
    )

    /**
     * Allows to specify age with an int and gender with a string.
     */
    @JvmOverloads constructor(
        knowledgeDegree: Double,
        randomGenerator: RandomGenerator,
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, F>,
        group: PGroup<T>? = null,
        age: Int,
        gender: String,
        danger: Molecule? = null
    ) : this(
        environment,
        randomGenerator,
        knowledgeDegree,
        group,
        Age.fromYears(age),
        Gender.fromString(gender),
        danger
    )

    /*
     * The cognitive part of the pedestrian, composition is used due to the lack
     * of multiple inheritance.
     */
    private val cognitive = CognitivePedestrianImpl(environment, randomGenerator, age, gender, danger, group)

    override val compliance = cognitive.compliance

    override fun probabilityOfHelping(
        toHelp: HeterogeneousPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation>
    ) = cognitive.probabilityOfHelping(toHelp)

    override fun dangerBelief() = cognitive.dangerBelief()

    override fun fear() = cognitive.fear()

    override fun wantsToEvacuate() = cognitive.wantsToEvacuate()

    override fun cognitiveCharacteristics() = cognitive.cognitiveCharacteristics()

    override fun influencialPeople() = cognitive.influencialPeople()
}
