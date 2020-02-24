package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator

/**
 * An orienting and cognitive pedestrian in an euclidean bidimensional space.
 *
 * @param E the type of edges of the environment's graph.
 */
class OrientingCognitivePedestrian2D<T, E : GraphEdge<ConvexPolygon>> @JvmOverloads constructor(
    knowledgeDegree: Double,
    rg: RandomGenerator,
    envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, E>,
    env: EuclideanPhysics2DEnvironment<T>,
    group: PedestrianGroup<T>? = null,
    override val age: Age,
    override val gender: Gender,
    danger: Molecule? = null
) : AbstractOrientingPedestrian2D<T, E>(knowledgeDegree, rg, envGraph, env, group), OrientingCognitivePedestrian<Euclidean2DPosition, Euclidean2DTransformation, Ellipse, GraphEdge<Ellipse>, T> {

    @JvmOverloads constructor(
        knowledgeDegree: Double,
        rg: RandomGenerator,
        envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, E>,
        env: EuclideanPhysics2DEnvironment<T>,
        group: PedestrianGroup<T>? = null,
        age: String,
        gender: String,
        danger: Molecule? = null
    ) : this(knowledgeDegree, rg, envGraph, env, group, Age.fromString(age), Gender.fromString(gender), danger)

    @JvmOverloads constructor(
        knowledgeDegree: Double,
        rg: RandomGenerator,
        envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, E>,
        env: EuclideanPhysics2DEnvironment<T>,
        group: PedestrianGroup<T>? = null,
        age: Int,
        gender: String,
        danger: Molecule? = null
    ) : this(knowledgeDegree, rg, envGraph, env, group, Age.fromYears(age), Gender.fromString(gender), danger)

    /*
     * The cognitive part of the pedestrian.
     */
    private val cognitive = CognitivePedestrianImpl(env, rg, age, gender, danger, group)
    private val shape = shape(env)

    init {
        senses += fieldOfView(env)
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
