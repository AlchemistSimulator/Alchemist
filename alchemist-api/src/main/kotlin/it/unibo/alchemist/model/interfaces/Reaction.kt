/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces

/**
 * @param <T>
 * The type which describes the concentration of a molecule
 *
 * A generic reaction. Every reaction in the system must implement
 * this interface.
</T> */
interface Reaction<T> : Actionable<T> {

    /**
     * The widest [Context] among [Condition]s, namely the
     * smallest [Context] in which the [Reaction] can read
     * informations.
     */
    val inputContext: Context

    /**
     * The widest [Context] among [Action]s, namely the
     * smallest context in which the [Reaction] can do
     * modifications.
     */
    val outputContext: Context

    /**
     * @return The [Node] in which this [Reaction] executes.
     */
    val node: Node<T>

    /**
     * This method allows to clone this reaction on a new node. It may result
     * useful to support runtime creation of nodes with the same reaction
     * programming, e.g. for morphogenesis.
     *
     * @param node
     * The node where to clone this Reaction
     * @param currentTime
     * the time at which the clone is created (required to correctly clone the [TimeDistribution]s)
     * @return the cloned action
     */
    fun cloneOnNewNode(node: Node<T>, currentTime: Time): Reaction<T>
}
