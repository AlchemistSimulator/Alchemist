/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.NetworkModel
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.observation.Observable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

/**
 * This model connects every node of [environment] to each and every other.
 */
class FullyConnected<T>(environment: Environment<T, *>) : NetworkModel<T> {

    private val sortedNodes = environment.nodes.map { it.sorted() }

    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> = sortedNodes.map { snapshot ->
        FullyConnectedNeighborhood(center, snapshot)
    }

    override fun toString(): String = checkNotNull(this::class.simpleName)

    /*
     * The neighbors of center among a sorted snapshot of the nodes. Neighborhoods built on the same snapshot are equal
     * without comparing their neighbors; any other neighborhood is compared by content, as by the standard ones.
     */
    private class FullyConnectedNeighborhood<T>(override val center: Node<T>, private val snapshot: List<Node<T>>) :
        Neighborhood<T> {
        override val neighbors: PersistentList<Node<T>> = (snapshot - center).toPersistentList()

        override fun equals(other: Any?): Boolean = this === other ||
            other is FullyConnectedNeighborhood<*> && center == other.center && snapshot === other.snapshot ||
            other is Neighborhood<*> && center == other.center && neighbors == other.neighbors

        override fun hashCode(): Int = arrayOf(center, neighbors).contentHashCode()

        override fun toString(): String = "${FullyConnectedNeighborhood::class.simpleName}($center)"
    }
}
