package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.GroupWithLeader
import it.unibo.alchemist.model.interfaces.Node

/**
 * A [Family] is modeled as a group of pedestrians with a leader.
 */
class Family<T>(
    comparator: Comparator<Node<T>> = Comparator { a, b -> a.id.compareTo(b.id) }
) : GenericGroup<T, Node<T>>(),
    GroupWithLeader<T, Node<T>> {

    override val leader: Node<T> =
        members.minWithOrNull(comparator) ?: throw IllegalStateException("Can't determine a leader.")
}
