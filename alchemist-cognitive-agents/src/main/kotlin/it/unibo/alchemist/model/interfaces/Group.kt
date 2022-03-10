package it.unibo.alchemist.model.interfaces

/**
 * A group of nodes.
 */
interface Group<T> : MutableList<Node<T>> {

    /**
     * The list of pedestrians belonging to this group.
     */
    val members: List<Node<T>> get() = this
}
