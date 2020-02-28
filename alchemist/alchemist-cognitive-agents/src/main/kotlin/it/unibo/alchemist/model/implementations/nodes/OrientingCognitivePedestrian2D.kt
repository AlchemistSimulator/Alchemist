package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator

/**
 * An orienting cognitive pedestrian in an [EuclideanPhysics2DEnvironment].
 *
 * @param N1 the type of nodes of the [environmentGraph].
 * @param E1 the type of edges of the [environmentGraph].
 * @param T  the concentration type.
 */
class OrientingCognitivePedestrian2D<N1 : ConvexPolygon, E1 : GraphEdge<N1>, T> @JvmOverloads constructor(
    knowledgeDegree: Double,
    randomGenerator: RandomGenerator,
    environmentGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, N1, E1>,
    environment: EuclideanPhysics2DEnvironment<T>,
    group: PedestrianGroup<T>? = null,
    override val age: Age,
    override val gender: Gender,
    danger: Molecule? = null
) : OrientingPedestrian2D<N1, E1, T>(knowledgeDegree, randomGenerator, environmentGraph, environment, group),
    OrientingCognitivePedestrian<Euclidean2DPosition, Euclidean2DTransformation, Ellipse, GraphEdge<Ellipse>, T> {

    @JvmOverloads constructor(
        knowledgeDegree: Double,
        randomGenerator: RandomGenerator,
        envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, N1, E1>,
        env: EuclideanPhysics2DEnvironment<T>,
        group: PedestrianGroup<T>? = null,
        age: String,
        gender: String,
        danger: Molecule? = null
    ) : this(knowledgeDegree, randomGenerator, envGraph, env, group, Age.fromString(age), Gender.fromString(gender), danger)

    @JvmOverloads constructor(
        knowledgeDegree: Double,
        randomGenerator: RandomGenerator,
        envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, N1, E1>,
        env: EuclideanPhysics2DEnvironment<T>,
        group: PedestrianGroup<T>? = null,
        age: Int,
        gender: String,
        danger: Molecule? = null
    ) : this(knowledgeDegree, randomGenerator, envGraph, env, group, Age.fromYears(age), Gender.fromString(gender), danger)

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
