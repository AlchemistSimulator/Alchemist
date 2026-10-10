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
import it.unibo.alchemist.model.Route

/**
 * Strategy describing how routing between two points happens.
 *
 * @param T concentration type
 * @param P position type
 */
fun interface RoutingStrategy<T, P : Position<P>> {
    /** Computes a route from [currentPos] to [finalPos]. */
    fun computeRoute(currentPos: P, finalPos: P): Route<P>

    /**
     * Returns a copy for [destination] and [reaction] when this strategy is stateful, or this object otherwise.
     */
    fun cloneIfNeeded(destination: Node<T>, reaction: NodeReaction<T>): RoutingStrategy<T, P> = this
}
