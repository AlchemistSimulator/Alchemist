/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import org.danilopianini.util.ArrayListSet
import org.danilopianini.util.Hashes
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.ListBackedSet
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets

/**
 * A basic implementation of the [Neighborhood] interface.
 */
class SimpleNeighborhood<T> private constructor(
    private val environment: Environment<T, *>,
    private val center: Node<T>,
    private val neighbors: ImmutableListSet<out Node<T>>
) : Neighborhood<T> {

    internal constructor(env: Environment<T, *>, center: Node<T>, neighbors: Iterable<Node<T>>)
        : this(env, center, ImmutableListSet.Builder<Node<T>>().addAll(neighbors).build())

    override fun clone() = SimpleNeighborhood(environment, center, ArrayListSet(neighbors))

    override fun contains(n: Int) = neighbors.map { it.id }.contains(n)

    override fun contains(n: Node<T>?) = neighbors.contains(n)

    override fun getBetweenRange(min: Double, max: Double): ListSet<out Node<T>> =
        environment.getPosition(center).let { centerPos -> ListBackedSet(neighbors.filter {
            Position.distanceTo(centerPos, environment.getPosition(it)) in min..max })
        }

    override fun getCenter() = center

    override fun getNeighborById(id: Int): Node<T> = neighbors.first { it.id == id }

    override fun getNeighborByNumber(num: Int): Node<T> = neighbors[num]

    override fun getNeighbors(): ListSet<out Node<T>> = ListSets.unmodifiableListSet(neighbors)

    override fun isEmpty() = neighbors.isEmpty()

    override fun iterator() = neighbors.iterator()

    override fun size() = neighbors.size

    override fun toString() = "$center links: $neighbors"

    override fun equals(other: Any?): Boolean =
        other is SimpleNeighborhood<*> && other.environment == environment && other.center == center && other.neighbors == neighbors

    override fun hashCode(): Int = Hashes.hash32(environment, center, neighbors)

    override fun add(node: Node<T>) = SimpleNeighborhood(environment, center, Iterable {
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
                    throw IllegalStateException("No other elements.")
                }
            }
        }
    })

    override fun remove(node: Node<T>) = if (this.contains(node)) {
        SimpleNeighborhood(environment, center, Iterable {
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
                        val result = lookahead!!
                        lookahead = updateLookAhead()
                        result
                    } else {
                        throw IllegalStateException("No other elements.")
                    }
            }
        })
    } else {
        throw IllegalArgumentException("$node not in $this")
    }
}