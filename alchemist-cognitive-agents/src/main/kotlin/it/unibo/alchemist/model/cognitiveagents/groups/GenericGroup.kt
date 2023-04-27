package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitiveagents.Group

/**
 * Basic implementation of a group.
 */
open class GenericGroup<T, N : Node<T>>(
    members: List<N> = mutableListOf(),
) : Group<T>, MutableList<Node<T>> by mutableListOf() {

    init {
        members.forEach { this.addMember(it) }
    }

    /**
     * adds [node] to the group if not already added.
     */
    fun addMember(node: N) = node !in this && add(node)

    /**
     * removes, if present, [node] from the group.
     */
    fun removeMember(node: N) = this.remove(node)
}
