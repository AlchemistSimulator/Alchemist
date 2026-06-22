/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import java.io.Serializable

/**
 * A neighborhood, namely the set of nodes to which a "central" node is connected to.
 *
 * @param <T> concentration type
</T> */
interface Neighborhood<T> {

    /**
     * Allows accessing the central node.
     *
     * @return the central node, namely the node whose neighbors are represented by
     * this structure.
     */
    val center: Node<T>

    /**
     * Allows directly accessing every node in the neighborhood.
     * A change of this List will be reflected in the neighborhood.
     *
     * @return the [java.util.List] of the neighbors
     */
    val neighbors: List<Node<T>>

    /**
     * @return true if this neighborhood has no neighbors
     */
    val isEmpty: Boolean get() = neighbors.isEmpty()
}
