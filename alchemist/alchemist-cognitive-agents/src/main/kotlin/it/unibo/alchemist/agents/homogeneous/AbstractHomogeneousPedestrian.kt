package it.unibo.alchemist.agents.homogeneous

import it.unibo.alchemist.agents.Pedestrian
import it.unibo.alchemist.characteristics.individual.Speed
import it.unibo.alchemist.groups.NoGroup
import it.unibo.alchemist.groups.Group
import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

abstract class AbstractHomogeneousPedestrian<T>(
    env: Environment<T, *>
) : AbstractNode<T>(env), Pedestrian<T> {

    override var membershipGroup: Group = NoGroup

    override val walkingSpeed = Speed.default

    override val runningSpeed = Speed.default * 3

    override fun createT(): T = TODO()
}