package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Node

/**
 * Basic implementation of a group.
 */
open class GenericGroup<T, N : Node<T>> : Group<T, N> {

    private val _members: MutableList<N> = mutableListOf()

    override val members: List<N>
        get() = _members

    override fun addMember(node: N): Group<T, N> = apply {
        if (!members.contains(node)) {
            _members.add(node)
        }
    }

    override fun removeMember(node: N): Group<T, N> = apply {
        _members.remove(node)
    }
}
