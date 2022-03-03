package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Node

/**
 * Basic implementation of a group.
 */
open class GenericGroup<T, N : Node<T>> : Group<T>, MutableList<Node<T>> by mutableListOf() {

    /**
     * adds [node] to the group if not already added.
     */
    fun addMember(node: N) = node !in this && add(node)

    /**
     * removes, if present, [node] from the group.
     */
    fun removeMember(node: N) = this.remove(node)
}
