/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction

/**
 * Computes a node's movement length toward a target.
 *
 * @param T concentration type
 * @param P position type
 */
fun interface SpeedSelectionStrategy<T, P : Position<out P>> {
    /**
     * Computes the movement length toward [target].
     */
    fun getNodeMovementLength(target: P?): Double

    /**
     * Returns a clone for [destination] and [reaction] when the strategy is stateful.
     */
    fun cloneIfNeeded(destination: Node<T>, reaction: Reaction<T>): SpeedSelectionStrategy<T, P> = this
}
