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
open class StraightLine<T, P : Position<P>> : RoutingStrategy<T, P> {
    override fun computeRoute(currentPos: P, finalPos: P): Route<P> = PolygonalChain(currentPos, finalPos)
}
