package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Environment

class CustomNode<T>(env: Environment<T, *>) : AbstractNode<T>(env) {
    override fun createT(): T = TODO()
}