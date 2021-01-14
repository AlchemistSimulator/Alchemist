package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.implementations.groups.Alone
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.InfluenceSphere
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic pedestrian.
 *
 * @param environment
 *          the environment inside which this pedestrian moves.
 */
abstract class AbstractHomogeneousPedestrian<T, P, A, F> @JvmOverloads constructor(
    open val environment: PhysicsEnvironment<T, P, A, F>,
    private val rg: RandomGenerator,
    group: PedestrianGroup<T, P, A>? = null
) : AbstractNode<T>(environment),
    Pedestrian<T, P, A>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    override fun createT(): T = TODO()

    override val membershipGroup: PedestrianGroup<T, P, A> by lazy {
        val pedestrianGroup = group?.addMember(this) as? PedestrianGroup<T, P, A>
        pedestrianGroup ?: Alone(this)
    }

    /**
     * The speed at which the pedestrian moves if it's walking.
     */
    protected open val walkingSpeed: Double = Speed.default

    /**
     * The speed at which the pedestrian moves if it's running.
     */
    protected open val runningSpeed: Double = Speed.default * 3

    /**
     * The list of influence spheres belonging to this pedestrian.
     */
    protected val senses: MutableList<InfluenceSphere> = mutableListOf()

    override fun speed() = rg.nextDouble(walkingSpeed, runningSpeed)
}
