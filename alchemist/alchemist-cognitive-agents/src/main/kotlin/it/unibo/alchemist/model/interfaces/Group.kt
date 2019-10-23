package it.unibo.alchemist.model.interfaces

/**
 * A group of nodes.
 */
interface Group<T, N : Node<T>> {

    /**
     * The list of pedestrians belonging to this group.
     */
    val members: List<N>

    /**
     * Whether a node belongs to this group or not.
     *
     * @param node The node to whom the membership must be checked.
     */
    fun contains(node: N): Boolean = members.contains(node)

    /**
     * Add a node in this group if not already part of it.
     *
     * @param node
     *          the node to add.
     */
    fun addMember(node: N): Group<T, N>

    /**
     * Remove a node from this group.
     *
     * @param node
     *          the node to remove.
     */
    fun removeMember(node: N): Group<T, N>
}
