/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies.routing

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Route
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.routes.PolygonalChain

/**
 * Routing strategy that computes a straight route from the current position to the final one.
 * In this implementation obstacles are ignored.
 *
 * @param <T> Concentration type
 * @param <P> position type
 */
class StraightLine<T, P : Position<P>> : RoutingStrategy<T, P> {
    override fun computeRoute(currentPos: P, finalPos: P): Route<P> = PolygonalChain(currentPos, finalPos)

    override fun toString(): String = "StraightLine"

    override fun equals(other: Any?): Boolean = other is StraightLine<*, *>

    override fun hashCode() = 1
}
