package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.cognitiveagents.groups.Group
import it.unibo.alchemist.model.cognitiveagents.groups.NoGroup
import it.unibo.alchemist.model.influencesphere.InfluenceSphere
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

/**
 * Implementation of a basic pedestrian.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 */
open class HomogeneousPedestrianImpl<T, P : Position<P>>(env: Environment<T, P>) : AbstractNode<T>(env), Pedestrian<T> {

    /**
     * The list of influence spheres belonging to this pedestrian.
     */
    protected val senses: MutableList<InfluenceSphere> = mutableListOf()

    override var membershipGroup: Group = NoGroup

    override val walkingSpeed = Speed.default

    override val runningSpeed = Speed.default * 3

    override fun createT(): T = TODO()
}