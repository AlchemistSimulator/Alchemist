package it.unibo.alchemist.agents.homogeneous

import it.unibo.alchemist.agents.Pedestrian
import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

abstract class AbstractHomogeneousPedestrian<T>(env: Environment<T, *>) : Pedestrian<T>, AbstractNode<T>(env) {

    override fun createT(): T = TODO()
}