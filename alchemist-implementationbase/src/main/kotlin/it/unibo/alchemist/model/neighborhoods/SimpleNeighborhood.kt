/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.neighborhoods

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

/**
 * A basic implementation of the [Neighborhood] interface.
 */
class SimpleNeighborhood<T, P : Position<P>> private constructor(
    private val environment: Environment<T, P>,
    override val center: Node<T>,
    override val neighbors: PersistentList<Node<T>>,
) : Neighborhood<T> {
    internal constructor(
        environment: Environment<T, P>,
        center: Node<T>,
        neighbors: Iterable<Node<T>>,
    ) : this(environment, center, neighbors.distinct().toPersistentList())

    override fun toString() = "$center links: $neighbors"

    override fun equals(other: Any?): Boolean = other is SimpleNeighborhood<*, *> &&
        other.environment == environment &&
        other.center == center &&
        other.neighbors == neighbors

    override fun hashCode(): Int = arrayOf(environment, center, neighbors).contentHashCode()

    override fun add(node: Node<T>): Neighborhood<T> =
        if (node in this) this else SimpleNeighborhood(environment, center, neighbors.add(node))

    override fun remove(node: Node<T>): Neighborhood<T> {
        require(node in this) {
            "$node not in $this"
        }
        return SimpleNeighborhood(environment, center, neighbors.remove(node))
    }
}
