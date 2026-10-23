/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

/**
 * A neighborhood, namely the set of nodes to which a "central" node is connected to.
 *
 * @param <T> concentration type
</T> */
interface Neighborhood<T> {

    /**
     * Returns a neighborhood with [node] among its neighbors.
     */
    operator fun plus(node: Node<T>): Neighborhood<T> = Neighborhood(center, neighbors + node)

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
    val neighbors: PersistentList<Node<T>>

    /**
     * @return true if this neighborhood has no neighbors
     */
    val isEmpty: Boolean get() = neighbors.isEmpty()

    /**
     * Returns a neighborhood without [node].
     */
    operator fun minus(node: Node<T>): Neighborhood<T> = Neighborhood(center, neighbors.removing(node))

    /**
     * Builders of [Neighborhood]s, whose neighbors are always sorted.
     */
    companion object {
        private operator fun <T> invoke(centerNode: Node<T>, sortedNeighbors: PersistentList<Node<T>>) =
            object : Neighborhood<T> {
                override val center: Node<T> get() = centerNode
                override val neighbors: PersistentList<Node<T>> get() = sortedNeighbors
                private val asString by lazy { "NeighborhoodOf(${center.id})=${neighbors.map { it.id }}" }

                override fun toString() = asString

                override fun equals(other: Any?): Boolean = this === other ||
                    other is Neighborhood<*> &&
                    other.center == center &&
                    other.neighbors == neighbors

                override fun hashCode(): Int = arrayOf(center, neighbors).contentHashCode()
            }

        /**
         * The neighborhood of [center] made of [neighbors], sorted.
         */
        operator fun <T> invoke(center: Node<T>, neighbors: Set<Node<T>>) =
            Neighborhood(center, neighbors.sorted().toPersistentList())

        /**
         * The neighborhood of [center] made of the distinct [neighbors], sorted.
         */
        operator fun <T> invoke(center: Node<T>, neighbors: Iterable<Node<T>>) = Neighborhood(center, neighbors.toSet())
    }
}
