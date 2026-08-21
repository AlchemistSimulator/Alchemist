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
 * A node-bound [Actionable]. Every reaction owns its schedule through [nextOccurrence].
 */
interface NodeReaction<T> : Actionable<T> {
    /**
     * @return The [Node] in which this [NodeReaction] executes.
     */
    val node: Node<T>

    /**
     * Clones this reaction's program on a new node, for example when nodes are created at runtime.
     *
     * The clone must be returned uninitialized, with a fresh [TimeDistribution]. The engine subsequently initializes
     * it and establishes a new [nextOccurrence]; a running occurrence or residual delay is never copied from the
     * source reaction.
     *
     * @param node
     * The node where to clone this Reaction
     * @param currentTime
     * the time at which the clone is created
     * @return the cloned action
     */
    fun cloneOnNewNode(node: Node<T>, currentTime: Time): NodeReaction<T>
}
