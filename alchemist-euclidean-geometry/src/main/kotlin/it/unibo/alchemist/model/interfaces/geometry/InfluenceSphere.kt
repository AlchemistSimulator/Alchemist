package it.unibo.alchemist.model.interfaces.geometry

import it.unibo.alchemist.model.interfaces.Node

/**
 * Area inside which nodes exert an influence on each other.
 */
typealias InfluenceSphere<T> = List<Node<T>>
