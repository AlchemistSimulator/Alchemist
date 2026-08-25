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
 * An operation performed when a [NodeReaction] executes.
 *
 * @param T concentration type
 */
interface Action<T> {
    /**
     * Creates an equivalent action for [node] and [reaction].
     */
    fun cloneAction(node: Node<T>, reaction: NodeReaction<T>): Action<T>

    /**
     * Applies this action to the model.
     */
    fun execute()
}
