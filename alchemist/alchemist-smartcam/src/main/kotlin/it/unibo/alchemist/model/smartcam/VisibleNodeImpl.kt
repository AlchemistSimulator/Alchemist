package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.VisibleNode

/**
 * Basic implementation of [VisibleNode]
 */
class VisibleNodeImpl<T, P : Position<P>>(
    override val node: Node<T>,
    override val position: P
) : VisibleNode<T, P> {

    override fun equals(other: Any?) = other is VisibleNodeImpl<*, *> && other.node == node

    override fun hashCode() = node.hashCode()

    override fun toString() = "Visible#${node.id}"
}