/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.NetworkModel
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observation.Observable

/**
 * A [NetworkModel] linking each node of [environment] to the nodes within [range] of it for which [isLinked] holds.
 * Neighborhoods follow the nodes within range: [isLinked] must depend on nothing that changes while they stay in range,
 * models whose links depend on further state extend [ObservingRangeBasedNetworkModel] instead.
 *
 * @param T concentration type
 * @param P position type
 * @property environment the environment whose nodes this model links
 * @property range the distance within which nodes may be linked
 */
abstract class RangeBasedNetworkModel<T, P : Position<P>>(
    protected val environment: Environment<T, P>,
    protected val range: Double,
) : NetworkModel<T> {

    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> =
        environment.observeNodesAround(center, range).map { neighbors ->
            Neighborhood(center, neighbors.filter { isLinked(center, it) })
        }

    final override fun toString(): String = "${javaClass.simpleName}($range)"

    /**
     * Whether [center] is linked to [neighbor], a node within range.
     */
    protected abstract fun isLinked(center: Node<T>, neighbor: Node<T>): Boolean
}
