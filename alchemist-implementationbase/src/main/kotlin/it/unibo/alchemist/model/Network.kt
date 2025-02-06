/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * A non-segmented network of [nodes].
 */
interface Network<T> {
    /**
     * The nodes that belong to this [Network].
     */
    val nodes: Set<Node<T>>

    /**
     * The diameter (the longest among the shortest paths) in the [Network].
     */
    val diameter: Double

    /**
     * Returns true whether the [Network] contains the [node] passed as input.
     */
    operator fun contains(node: Node<T>): Boolean = nodes.contains(node)

    /**
     * Returns true whether the [Network] contains at least one of the [nodes] passed as input.
     */
    fun containsAtLeastOneOf(nodes: Set<Node<T>>): Boolean = nodes.any { it in this }

    /**
     * Returns a new [Network] obtained by adding the [otherNetwork] to this one.
     */
    operator fun plus(otherNetwork: Network<T>): Network<T>
}
