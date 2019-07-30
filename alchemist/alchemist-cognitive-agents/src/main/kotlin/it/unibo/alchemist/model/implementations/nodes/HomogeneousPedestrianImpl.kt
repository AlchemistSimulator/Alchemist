package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.cognitiveagents.groups.Group
import it.unibo.alchemist.model.cognitiveagents.groups.NoGroup
import it.unibo.alchemist.model.implementations.actions.utils.nextDouble
import it.unibo.alchemist.model.influencesphere.InfluenceSphere
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic pedestrian.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 */
open class HomogeneousPedestrianImpl<T, P : Position<P>>(
    env: Environment<T, P>,
    private val rg: RandomGenerator
) : AbstractNode<T>(env), Pedestrian<T> {

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

    override var membershipGroup: Group = NoGroup

    override fun speed() = rg.nextDouble(walkingSpeed, runningSpeed)

    override fun createT(): T = TODO()
}