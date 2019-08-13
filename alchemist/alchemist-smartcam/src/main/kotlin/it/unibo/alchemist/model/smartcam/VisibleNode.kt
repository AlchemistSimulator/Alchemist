package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position

/**
 * Represents a node seen by the [See] action.
 */
data class VisibleNode<T, P : Position<P>>(
    /**
     * The node seen.
     */
    val node: Node<T>,
    private val env: Environment<T, P>
) {

    /**
     * Identifier.
     */
    val id = node.id

    /**
     * Absolute position.
     */
    val position: P = env.getPosition(node)

    override fun equals(other: Any?): Boolean {
        if (other is VisibleNode<*, *>) {
            return other.id == id
        }
        return false
    }

    override fun hashCode() =
        id.hashCode()

    override fun toString() =
        "Visible#$id"
}