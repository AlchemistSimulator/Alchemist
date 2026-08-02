/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import kotlinx.collections.immutable.ImmutableList

/**
 * A neighborhood, namely the set of nodes to which a "central" node is connected to.
 *
 * @param <T> concentration type
</T> */
interface Neighborhood<T> : Iterable<Node<T>> {

    /**
     * Returns a neighborhood with [node] among its neighbors.
     */
    fun add(node: Node<T>): Neighborhood<T>

    /**
     * Checks whether [node] belongs to this neighborhood.
     */
    operator fun contains(node: Node<T>): Boolean = node in neighbors

    /**
     * Allows accessing the central node.
     *
     * @return the central node, namely the node whose neighbors are represented by
     * this structure.
     */
    val center: Node<T>

    /**
     * An ordered, read-only view of every node in the neighborhood.
     */
    val neighbors: ImmutableList<Node<T>>

    /**
     * @return true if this neighborhood has no neighbors
     */
    val isEmpty: Boolean get() = neighbors.isEmpty()

    /**
     * Returns a neighborhood without [node].
     */
    fun remove(node: Node<T>): Neighborhood<T>

    /**
     * Returns the number of neighbors.
     */
    fun size(): Int = neighbors.size

    override fun iterator(): Iterator<Node<T>> = neighbors.iterator()
}
