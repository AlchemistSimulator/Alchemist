/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.adapters

import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.rx.model.adapters.ObservableNode.NodeExtension.asObservableNode
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet.Companion.toObservableSet
import it.unibo.alchemist.rx.model.observation.ObservableSet
import org.danilopianini.util.ArrayListSet
import org.danilopianini.util.ListSet

// TODO: evaluate a mutable version feasibility
/**
 * Represents an observable neighborhood of nodes in a space, providing observation capabilities to track changes
 * in its contents. It wraps a traditional [Neighborhood] and makes its nodes observable using [ObservableNode].
 * The neighborhood's center and neighbors are exposed as observable entities.
 *
 * > **Implementation Notes**: due to some bugs and limitation of the Kotlin compiler with java interop,
 * the compiler struggles in resolving the [Cloneable][java.lang.Cloneable] interface when delegating to [origin].
 * For this reason delegation is made explicit for methods like [isEmpty] or [observableSize].
 *
 * @param T The type of the values associated with the nodes in the neighborhood.
 * @property origin The underlying [Neighborhood] instance providing the base functionality.
 * @property backingSet An [ObservableSet] containing the observable representation of the neighborhood's nodes.
 */
class ObservableNeighborhood<T>(
    private val origin: Neighborhood<T>,
    private val backingSet: ObservableSet<ObservableNode<T>> = origin.neighbors.map {
        it.asObservableNode()
    }.toObservableSet(),
) : Neighborhood<T>,
    ObservableSet<ObservableNode<T>> by backingSet {

    private val _center = origin.center.asObservableNode()

    // TODO: optimise creation maybe
    override fun add(node: Node<T>): ObservableNeighborhood<T> = ObservableNeighborhood(origin.add(node))

    override fun remove(node: Node<T>): ObservableNeighborhood<T> = ObservableNeighborhood(origin.remove(node))

    override fun getCenter(): Node<T> = _center

    override fun getNeighbors(): ListSet<ObservableNode<T>> = ArrayListSet(backingSet.toSet())

    override fun iterator(): MutableIterator<ObservableNode<T>> = backingSet.toSet().toMutableSet().iterator()

    override fun contains(n: Node<T>): Boolean = origin.contains(n)

    override fun isEmpty(): Boolean = origin.isEmpty

    override fun size(): Int = origin.size()

    override fun toString(): String = "ObservableNeighborhood(center=$center, neighbors=${toSet()})"

    /**
     * Provides an extension function to transform a `Neighborhood` into an `ObservableNeighborhood`.
     */
    companion object {
        /**
         * Transforms a [Neighborhood] into an [ObservableNeighborhood], enabling observation capabilities
         * for the neighborhood's center and its neighbors. The returned observable representation reflects
         * any changes to the original neighborhood.
         *
         * @return an [ObservableNeighborhood] wrapping the current [Neighborhood].
         */
        fun <T> Neighborhood<T>.asObservable(): ObservableNeighborhood<T> = ObservableNeighborhood(this)
    }
}
