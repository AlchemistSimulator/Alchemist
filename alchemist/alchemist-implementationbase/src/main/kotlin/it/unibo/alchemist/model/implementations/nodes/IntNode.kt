package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Environment

open class IntNode(env: Environment<*>) : GenericNode<Int>(env) {
    override fun createT() = 0
}