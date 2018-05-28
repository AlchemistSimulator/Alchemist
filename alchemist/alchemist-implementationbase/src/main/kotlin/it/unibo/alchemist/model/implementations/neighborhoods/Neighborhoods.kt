package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node

class Neighborhoods {
    companion object {
        @JvmStatic @JvmOverloads fun <T> make(
            env: Environment<T>,
            center: Node<T>,
            neighbors: Iterable<Node<T>> = mutableListOf()
        ) = SimpleNeighborhood(env, center, neighbors)
    }
}