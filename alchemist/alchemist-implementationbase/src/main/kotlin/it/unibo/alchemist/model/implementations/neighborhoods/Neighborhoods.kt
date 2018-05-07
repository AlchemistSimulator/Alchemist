@file:JvmName("Neighborhoods")
package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import java.util.Collections

@JvmOverloads
fun <T> make(env: Environment<T>,
             center: Node<T>,
             neighbors: Iterable<Node<T>> = Collections.emptyList()) =
        SimpleNeighborhood(env, center, neighbors)
