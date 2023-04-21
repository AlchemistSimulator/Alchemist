/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.util.BugReporting.reportBug
import org.danilopianini.util.ArrayListSet
import org.danilopianini.util.Hashes
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets

/**
 * A basic implementation of the [Neighborhood] interface.
 */
class SimpleNeighborhood<T, P : Position<P>> private constructor(
    private val environment: Environment<T, P>,
    private val center: Node<T>,
    private val neighbors: ImmutableListSet<out Node<T>>,
) : Neighborhood<T> {

    internal constructor(
        environment: Environment<T, P>,
        center: Node<T>,
        neighbors: Iterable<Node<T>>,
    ) : this(environment, center, ImmutableListSet.Builder<Node<T>>().addAll(neighbors).build())

    override fun clone() = SimpleNeighborhood(environment, center, ArrayListSet(neighbors))

    override fun contains(node: Node<T>?) = neighbors.contains(node)

    override fun getCenter() = center

    override fun getNeighbors(): ListSet<out Node<T>> = ListSets.unmodifiableListSet(neighbors)

    override fun isEmpty() = neighbors.isEmpty()

    override fun iterator() = neighbors.iterator()

    override fun size() = neighbors.size

    override fun toString() = "$center links: $neighbors"

    override fun equals(other: Any?): Boolean = other is SimpleNeighborhood<*, *> &&
        other.environment == environment &&
        other.center == center &&
        other.neighbors == neighbors

    override fun hashCode(): Int = Hashes.hash32(environment, center, neighbors)

    override fun add(node: Node<T>) = SimpleNeighborhood(
        environment,
        center,
        Iterable {
            object : Iterator<Node<T>> {
                val previousNodes = neighbors.iterator()
                var nodeReady = true
                override fun hasNext() = nodeReady
                override fun next() = if (previousNodes.hasNext()) {
                    previousNodes.next()
                } else {
                    if (nodeReady) {
                        nodeReady = false
                        node
                    } else {
                        throw NoSuchElementException("No other elements.")
                    }
                }
            }
        },
    )

    override fun remove(node: Node<T>): Neighborhood<T> {
        require(node in this) {
            "$node not in $this"
        }
        return SimpleNeighborhood(
            environment,
            center,
            Iterable {
                object : Iterator<Node<T>> {
                    val base = neighbors.iterator()
                    var lookahead = updateLookAhead()
                    fun updateLookAhead(): Node<T>? =
                        if (base.hasNext()) {
                            val maybeNext = base.next()
                            if (maybeNext == node) {
                                updateLookAhead()
                            } else {
                                maybeNext
                            }
                        } else {
                            null
                        }
                    override fun hasNext() = lookahead !== null
                    override fun next() =
                        if (hasNext()) {
                            val result = lookahead ?: reportBug(
                                "Neighborhood iterator failure in ${this::class.qualifiedName}",
                                mapOf(
                                    "base" to base,
                                    "lookahead" to lookahead,
                                    "hasNext" to hasNext(),
                                ),
                            )
                            lookahead = updateLookAhead()
                            result
                        } else {
                            throw NoSuchElementException("No other elements.")
                        }
                }
            },
        )
    }
}
