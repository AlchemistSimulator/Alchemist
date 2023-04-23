package it.unibo.alchemist.test

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.nodes.GenericNode

class CustomNode<T>(environment: Environment<T, *>) : GenericNode<T>(environment) {
    override fun createT(): T = TODO()
}
