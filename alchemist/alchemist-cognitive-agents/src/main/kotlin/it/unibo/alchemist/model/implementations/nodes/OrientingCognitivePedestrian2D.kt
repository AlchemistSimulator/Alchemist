package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.OrientingCognitivePedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator

/**
 * A cognitive [OrientingPedestrian2D] in an [EuclideanPhysics2DEnvironment].
 *
 * @param T the concentration type.
 * @param M the type of nodes of the [environmentGraph].
 * @param F the type of edges of the [environmentGraph].
 */
class OrientingCognitivePedestrian2D<T, M : ConvexPolygon, F : GraphEdge<M>> @JvmOverloads constructor(
    knowledgeDegree: Double,
    randomGenerator: RandomGenerator,
    environmentGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, M, F>,
    environment: EuclideanPhysics2DEnvironment<T>,
    group: PedestrianGroup<T>? = null,
    override val age: Age,
    override val gender: Gender,
    danger: Molecule? = null
) : OrientingPedestrian2D<T, M, F>(knowledgeDegree, randomGenerator, environmentGraph, environment, group),
    OrientingCognitivePedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Ellipse, GraphEdge<Ellipse>> {

    /**
     * Allows to specify age and gender with a string.
     */
    @JvmOverloads constructor(
        knowledgeDegree: Double,
        randomGenerator: RandomGenerator,
        envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, M, F>,
        env: EuclideanPhysics2DEnvironment<T>,
        group: PedestrianGroup<T>? = null,
        age: String,
        gender: String,
        danger: Molecule? = null
    ) : this(
            knowledgeDegree,
            randomGenerator,
            envGraph,
            env,
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
        envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, M, F>,
        env: EuclideanPhysics2DEnvironment<T>,
        group: PedestrianGroup<T>? = null,
        age: Int,
        gender: String,
        danger: Molecule? = null
    ) : this(
            knowledgeDegree,
            randomGenerator,
            envGraph,
            env,
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
    private val shape = shape(environment)

    init {
        senses += fieldOfView(environment)
    }

    override val compliance = cognitive.compliance

    /**
     */
    override fun getShape() = shape

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>) = cognitive.probabilityOfHelping(toHelp)

    override fun dangerBelief() = cognitive.dangerBelief()

    override fun fear() = cognitive.fear()

    override fun wantsToEvacuate() = cognitive.wantsToEvacuate()

    override fun cognitiveCharacteristics() = cognitive.cognitiveCharacteristics()

    override fun influencialPeople() = cognitive.influencialPeople()
}
