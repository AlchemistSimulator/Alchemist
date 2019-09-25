package it.unibo.alchemist.model.interfaces

/**
 * A group with a special member acting as a leader.
 */
interface GroupWithLeader<T, N : Node<T>> : Group<T, N> {

    /**
     * The leader of the group.
     */
    val leader: N
}