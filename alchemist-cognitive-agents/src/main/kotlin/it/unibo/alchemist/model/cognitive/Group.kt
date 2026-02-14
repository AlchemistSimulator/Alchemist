package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Node

/**
 * Represents a group of nodes (pedestrians) that can be treated collectively.
 *
 * @param T the concentration type used by nodes in the group.
 */
interface Group<T> : MutableList<Node<T>> {
    /**
     * The list of nodes that belong to this group.
     *
     * @return a [List] of [Node] instances representing the group's members.
     */
    val members: List<Node<T>> get() = this
}
