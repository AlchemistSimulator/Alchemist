package it.unibo.alchemist.agents.homogeneous

import it.unibo.alchemist.agents.Pedestrian
import it.unibo.alchemist.groups.NoGroup
import it.unibo.alchemist.groups.Group
import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

const val DEFAULT_WALKING_SPEED = 1.0 // m/s

abstract class AbstractHomogeneousPedestrian<T>(
    env: Environment<T, *>
) : AbstractNode<T>(env), Pedestrian<T> {

    override var membershipGroup: Group = NoGroup

    override val walkingSpeed = DEFAULT_WALKING_SPEED

    override val runningSpeed = DEFAULT_WALKING_SPEED * 3

    override fun createT(): T = TODO()
}