package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.cognitiveagents.groups.NoGroup
import it.unibo.alchemist.model.cognitiveagents.groups.Group
import it.unibo.alchemist.model.interfaces.Environment

abstract class AbstractHomogeneousPedestrian<T>(
    env: Environment<T, *>
) : AbstractNode<T>(env), Pedestrian<T> {

    override var membershipGroup: Group = NoGroup

    override val walkingSpeed = Speed.default

    override val runningSpeed = Speed.default * 3

    override fun createT(): T = TODO()
}