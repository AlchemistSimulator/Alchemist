package it.unibo.alchemist.model.interfaces.geometry

import it.unibo.alchemist.model.Node

/**
 * Area inside which nodes exert an influence on each other.
 */
interface InfluenceSphere<T> {
    /**
     * List of influential nodes. (e.g. nodes withing a field of view).
     */
    fun influentialNodes(): List<Node<T>>
}
