/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model

/**
 * @param <T>
 * The type which describes the concentration of a molecule
 *
 * A node-bound [Reaction]. Every reaction owns its schedule through [nextOccurrence].
 */
interface NodeReaction<T> : Reaction<T> {
    /**
     * @return The [Node] in which this [NodeReaction] executes.
     */
    val node: Node<T>

    /**
     * Clones this reaction's program on a new node, for example when nodes are created at runtime.
     *
     * The clone must be returned uninitialized. A [TimeDistributedReaction] receives a fresh [TimeDistribution]
     * and schedule. The engine subsequently initializes the clone, and no consumed occurrence or residual delay is
     * resurrected. Host-neutral reactions are deliberately not cloned with a node.
     *
     * @param node
     * The node where to clone this Reaction
     * @param currentTime
     * the time at which the clone is created
     * @return the cloned reaction
     */
    fun cloneOnNewNode(node: Node<T>, currentTime: Time): NodeReaction<T>
}
