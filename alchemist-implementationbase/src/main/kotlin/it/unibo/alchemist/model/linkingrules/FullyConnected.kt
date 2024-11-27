/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.linkingrules

import com.google.common.collect.Iterators
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.util.BugReporting
import org.danilopianini.util.ListSet

/**
 * This rule connects each and every node to each and every other.
 */
class FullyConnected<T, P : Position<P>> : LinkingRule<T, P> {
    override fun isLocallyConsistent() = true

    override fun computeNeighborhood(
        center: Node<T>,
        environment: Environment<T, P>,
    ) = object :
        Neighborhood<T> {
        override fun contains(node: Node<T>?) = node != center

        override fun getCenter() = center

        override fun isEmpty() = environment.nodeCount <= 1

        override fun getNeighbors() =
            object : ListSet<Node<T>> {
                override fun get(index: Int) = BugReporting.reportBug("Not implemented")

                override fun indexOf(element: Node<T>?) = BugReporting.reportBug("Not implemented")

                override fun lastIndexOf(element: Node<T>?) = BugReporting.reportBug("Not implemented")

                override fun add(
                    index: Int,
                    element: Node<T>?,
                ) = BugReporting.reportBug("Not implemented")

                override fun addAll(
                    index: Int,
                    elements: Collection<Node<T>>,
                ) = BugReporting.reportBug("Not implemented")

                override fun listIterator() = BugReporting.reportBug("Not implemented")

                override fun listIterator(index: Int) = BugReporting.reportBug("Not implemented")

                override fun removeAt(index: Int) = BugReporting.reportBug("Not implemented")

                override fun set(
                    index: Int,
                    element: Node<T>?,
                ) = BugReporting.reportBug("Not implemented")

                override fun subList(
                    fromIndex: Int,
                    toIndex: Int,
                ) = BugReporting.reportBug("Not implemented")

                override fun add(element: Node<T>?) = BugReporting.reportBug("Not implemented")

                override fun addAll(elements: Collection<Node<T>>) = BugReporting.reportBug("Not implemented")

                override fun clear() = BugReporting.reportBug("Not implemented")

                override fun iterator() = Iterators.filter(environment.nodes.iterator()) { it != center }

                override fun remove(element: Node<T>?) = BugReporting.reportBug("Not implemented")

                override fun removeAll(elements: Collection<Node<T>>) =
                    BugReporting.reportBug("Not implemented")

                override fun retainAll(elements: Collection<Node<T>>) =
                    BugReporting.reportBug("Not implemented")

                override val size = environment.nodeCount - 1

                override fun contains(element: Node<T>?) = element != center && environment.contains(element)

                override fun containsAll(elements: Collection<Node<T>>) = elements.all { contains(it) }

                override fun isEmpty() = environment.nodeCount == 1
            }

        override fun remove(node: Node<T>?) = this

        override fun add(node: Node<T>?) = this

        override fun iterator() = neighbors.iterator()

        override fun size() = environment.nodeCount - 1
    }
}
