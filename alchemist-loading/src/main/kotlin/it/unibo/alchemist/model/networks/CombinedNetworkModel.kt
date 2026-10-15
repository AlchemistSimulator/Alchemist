/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import arrow.core.getOrElse
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.NetworkModel
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observation.Observable

/**
 * A network model that combines multiple [subRules], all bound to the same environment.
 * If any mandates a link, such link is created (union of all links).
 */
class CombinedNetworkModel<T>(val subRules: List<NetworkModel<T>>) : NetworkModel<T> {
    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> =
        subRules.map { it.neighborhoodOf(center) }.combineLatest { neighborhoods ->
            Neighborhood(center, neighborhoods.flatMap { it.neighbors }.toSet())
        }.map { combined -> combined.getOrElse { Neighborhood(center, emptySet()) } }

    override fun toString(): String = "${javaClass.simpleName}($subRules)"
}
