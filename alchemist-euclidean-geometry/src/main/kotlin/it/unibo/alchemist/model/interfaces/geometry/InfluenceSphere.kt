package it.unibo.alchemist.model.interfaces.geometry

import it.unibo.alchemist.model.interfaces.Node

/**
 * Area inside which nodes exert an influence on each other.
 */
interface InfluenceSphere<T> {
    fun influentialNodes(): List<Node<T>>
}
