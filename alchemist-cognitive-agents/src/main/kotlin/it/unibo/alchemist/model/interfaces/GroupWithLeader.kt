package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.Node

/**
 * A group with a special member acting as a leader.
 */
interface GroupWithLeader<T, N : Node<T>> : Group<T> {

    /**
     * The leader of the group.
     */
    val leader: N
}
