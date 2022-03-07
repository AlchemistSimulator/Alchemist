package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.nodes.GenericNode
import it.unibo.alchemist.model.interfaces.Environment

class CustomNode<T>(environment: Environment<T, *>) : GenericNode<T>(environment) {
    override fun createT(): T = TODO()
}
