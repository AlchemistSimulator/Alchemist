package it.unibo.alchemist.model.implementations.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.interfaces.MapEnvironment
import it.unibo.alchemist.model.linkingrules.AbstractLocallyConsistentLinkingRule
import kotlin.math.abs
import kotlin.math.min

/**
 * This rule connects nodes that are within [maxRange] distance, but only if there are not too many obstacles
 * separating their line of sight.
 * The base idea is that line-of-sight distance and routing distance should not differ "too much",
 * as captured by [tolerance].
 * [tolerance] measures the maximum allowed relative difference between the line-of-sight and the route distance.
 * It's default 0.1 (10%) means that if LOS-distance is more than 10% shorter than route-distance, then the nodes are
 * considered disconnected.
 * Route distance is taken two-ways (to account for one-way roads) and the shortest one is considered
 * (wireless signals do not need to follow one-way roads).
 */
class ConnectIfInLineOfSigthOnMap<T> @JvmOverloads constructor(
    val maxRange: Double,
    val tolerance: Double = 0.1,
) : AbstractLocallyConsistentLinkingRule<T, GeoPosition>() {

    override fun computeNeighborhood(center: Node<T>, environment: Environment<T, GeoPosition>): Neighborhood<T> {
        require(environment is MapEnvironment<T, *, *>) {
            "Cannot operate of environments of type " + environment::class.simpleName
        }
        val inRange = environment.getNodesWithinRange(center, maxRange)
            .filter { target ->
                val losDistance = environment.getDistanceBetweenNodes(center, target)
                val outbound = environment.computeRoute(center, target)
                val inbound = environment.computeRoute(target, center)
                val shortest = min(outbound.length(), inbound.length())
                shortest <= maxRange && abs(shortest - losDistance) < tolerance * shortest
            }
        return Neighborhoods.make(environment, center, inRange)
    }
}
