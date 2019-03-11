/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.linkingrules

import com.google.common.collect.Iterators
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.LinkingRule
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import org.danilopianini.util.ListSet

class FullyConnected<T, P : Position<P>>() : LinkingRule<T, P> {
    override fun isLocallyConsistent() = true
    override fun computeNeighborhood(center: Node<T>, env: Environment<T, P>) = object : Neighborhood<T> {

        override fun contains(n: Node<T>?) = n != center

        override fun getCenter() = center

        override fun isEmpty() = env.nodesNumber <= 1

        override fun getNeighbors() = object : ListSet<Node<T>> {
            override fun get(index: Int) = TODO()
            override fun indexOf(element: Node<T>?) = TODO()
            override fun lastIndexOf(element: Node<T>?) = TODO()
            override fun add(index: Int, element: Node<T>?) = TODO()
            override fun addAll(index: Int, elements: Collection<Node<T>>) = TODO()
            override fun listIterator() = TODO()
            override fun listIterator(index: Int) = TODO()
            override fun removeAt(index: Int) = TODO()
            override fun set(index: Int, element: Node<T>?) = TODO()
            override fun subList(fromIndex: Int, toIndex: Int) = TODO()
            override fun add(element: Node<T>?) = TODO()
            override fun addAll(elements: Collection<Node<T>>) = TODO()
            override fun clear() = TODO()
            override fun iterator() = Iterators.filter(env.nodes.iterator()) { it != center }
            override fun remove(element: Node<T>?) = TODO()
            override fun removeAll(elements: Collection<Node<T>>) = TODO()
            override fun retainAll(elements: Collection<Node<T>>) = TODO()
            override val size = env.nodesNumber - 1
            override fun contains(element: Node<T>?) = element != center && env.contains(element)
            override fun containsAll(elements: Collection<Node<T>>) = elements.all { contains(it) }
            override fun isEmpty() = env.nodesNumber == 1
        }

        override fun remove(node: Node<T>?) = this

        override fun add(node: Node<T>?) = this

        override fun iterator() = neighbors.iterator()

        override fun getNeighborByNumber(num: Int) = TODO()

        override fun size() = env.nodesNumber - 1
    }
}
