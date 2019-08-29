package it.unibo.alchemist.model.interfaces

/**
 * Represents a node seen by the [it.unibo.alchemist.model.implementations.actions.See] action.
 */
interface VisibleNode<T, P : Position<P>> {
    /**
     * The node seen.
     */
    val node: Node<T>

    /**
     * The position of the node
     */
    val position: P
}