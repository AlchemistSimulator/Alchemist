package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

class CustomNode<T>(environment: Environment<T, *>) : AbstractNode<T>(environment) {
    override fun createT(): T = TODO()
}
