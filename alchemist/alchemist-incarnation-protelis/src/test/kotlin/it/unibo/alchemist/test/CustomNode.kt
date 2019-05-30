package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

class CustomNode<T>(env: Environment<T, *>) : AbstractNode<T>(env) {
    override fun createT(): T = TODO()
}