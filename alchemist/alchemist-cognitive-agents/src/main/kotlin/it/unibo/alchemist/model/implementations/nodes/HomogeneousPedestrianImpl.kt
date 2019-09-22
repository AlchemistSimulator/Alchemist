package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.implementations.groups.Alone
import it.unibo.alchemist.model.implementations.utils.nextDouble
import it.unibo.alchemist.model.influencesphere.InfluenceSphere
import it.unibo.alchemist.model.interfaces.*
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic pedestrian.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 */
open class HomogeneousPedestrianImpl<T, P : Position<P>> @JvmOverloads constructor(
    env: Environment<T, P>,
    private val rg: RandomGenerator,
    group: PedestrianGroup<T>? = null
) : AbstractNode<T>(env), Pedestrian<T> {

    override fun createT(): T = TODO()

    override val membershipGroup: Group<T, Pedestrian<T>> by lazy {
        group?.addMember(this) ?: Alone(this)
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