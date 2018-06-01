package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node

class Neighborhoods {
    companion object {
        /**
         * Creates a [SimpleNeighborhood].
         *
         * @param env The environment of the neighborhood.
         * @param center The center of the neighborhood.
         * @param neighbors The neighbors in the neighborhood, defaults to empty.
         *
         * @return The newly created [SimpleNeighborhood].
         */
        @JvmStatic @JvmOverloads fun <T> make(
            env: Environment<T>,
            center: Node<T>,
            neighbors: Iterable<Node<T>> = mutableListOf()
        ) = SimpleNeighborhood(env, center, neighbors)
    }
}