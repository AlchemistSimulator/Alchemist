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
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observation.Observable

/**
 * A network model that combines multiple [subRules] all bound to the same environment.
 * If any mandates a link, such link is created (union of all links).
 */
class CombinedNetworkModel<T>(subRules: List<NetworkModel<T>>) : NetworkModel<T> {

    constructor(
        subRule1: NetworkModel<T>,
        subRule2: NetworkModel<T>,
        vararg otherSubRules: NetworkModel<T>,
    ) : this(listOf(subRule1, subRule2, *otherSubRules))

    init {
        require(subRules.size >= 2) { "At least two sub-rules must be provided" }
    }

    /*
     * The union of links is associative and idempotent: nested combinations are flattened and duplicates dropped.
     * Models linking nothing add nothing, and a fully connected model makes the whole combination fully connected.
     */
    private val subRules: List<NetworkModel<T>> = subRules.flatten()
        .distinct()
        .filterNot { it is NoLinks<*> }
        .let { models -> models.firstOrNull { it is FullyConnected<*> }?.let(::listOf) ?: models }

    // A combination of a single model links as that model does, and one of no models links nothing.
    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> = when (subRules.size) {
        0 -> observe(Neighborhood(center, emptySet()))
        1 -> subRules.single().neighborhoodOf(center)
        else -> subRules.map { it.neighborhoodOf(center) }
            .combineLatest { neighborhoods -> Neighborhood(center, neighborhoods.flatMap { it.neighbors }.toSet()) }
            .map { combined -> combined.getOrElse { Neighborhood(center, emptySet()) } }
    }

    override fun toString(): String = "${javaClass.simpleName}($subRules)"

    private fun List<NetworkModel<T>>.flatten(): List<NetworkModel<T>> = flatMap {
        when (it) {
            is CombinedNetworkModel<T> -> it.subRules.flatten()
            else -> listOf(it)
        }
    }
}
