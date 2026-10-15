/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.networks.ObservingRangeBasedNetworkModel
import it.unibo.alchemist.model.observation.Observable
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
class ConnectIfInLineOfSigthOnMap<T>
@JvmOverloads
constructor(
    environment: Environment<T, GeoPosition>,
    val maxRange: Double,
    val tolerance: Double = 0.1,
) : ObservingRangeBasedNetworkModel<T, GeoPosition, GeoPosition>(environment, maxRange) {

    private val map: MapEnvironment<T, *, *> = requireNotNull(environment as? MapEnvironment<T, *, *>) {
        "Cannot operate of environments of type " + environment::class.simpleName
    }

    override fun isLinked(
        center: Node<T>,
        centerInput: GeoPosition,
        neighbor: Node<T>,
        neighborInput: GeoPosition,
    ): Boolean {
        val losDistance = centerInput.distanceTo(neighborInput)
        val outbound = map.computeRoute(center, neighbor)
        val inbound = map.computeRoute(neighbor, center)
        val shortest = min(outbound.length(), inbound.length())
        return shortest <= maxRange && abs(shortest - losDistance) < tolerance * shortest
    }

    // Routes depend on the positions of both nodes.
    override fun observeInput(node: Node<T>): Observable<GeoPosition> = map.positionOf(node)
}
