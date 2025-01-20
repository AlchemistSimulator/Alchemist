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
 * The [nodes] inside a subnetwork and relative [diameter].
 */
data class Subnetwork<T>(
    val nodes: Set<Node<T>>,
    val diameter: Int,
) {
    fun contains(node: Node<T>): Boolean = nodes.contains(node)
}
