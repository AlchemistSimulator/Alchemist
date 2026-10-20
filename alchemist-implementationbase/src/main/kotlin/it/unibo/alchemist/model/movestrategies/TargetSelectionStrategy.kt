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
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position

/**
 * This interface models a strategy for selecting positions where to move.
 *
 * @param T Concentration type
 * @param P Position type
 */
interface TargetSelectionStrategy<T, P : Position<out P>> {
    /**
     * The next target where the [Node] is directed.
     */
    val target: P

    /**
     * Clones this strategy, if needed, for the [destination] node and its [reaction].
     * Returns a copy of the strategy if the strategy is stateful, and this object otherwise:
     * the default implementation assumes a stateless strategy.
     */
    fun cloneIfNeeded(destination: Node<T>, reaction: NodeReaction<T>): TargetSelectionStrategy<T, P> = this
}
