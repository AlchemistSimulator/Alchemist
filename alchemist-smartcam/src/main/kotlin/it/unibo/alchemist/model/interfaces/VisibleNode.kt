package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position

/**
 * Represents a node seen by the [it.unibo.alchemist.model.implementations.actions.CameraSee] action.
 */
interface VisibleNode<T, P : Position<P>> {
    /**
     * The node seen.
     */
    val node: Node<T>

    /**
     * The position of the node.
     */
    val position: P
}
