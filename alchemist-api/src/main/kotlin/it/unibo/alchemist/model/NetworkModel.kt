/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.observation.Observable

/**
 * A policy that links the nodes of an environment, observing the state the links depend on (such as positions or
 * contents) and publishing the neighborhood of each node whenever it changes.
 * Network models are bound to the environment whose nodes they link.
 *
 * @param T concentration type
 */
interface NetworkModel<T> {

    /**
     * The neighborhood of this node, as provided by [neighborhoodOf].
     */
    val Node<T>.neighborhood: Observable<Neighborhood<T>> get() = neighborhoodOf(this)

    /**
     * The neighborhood of [center], a node of the environment of this model, as an observable of immutable snapshots
     * that emits whenever the neighborhood changes.
     */
    fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>>
}
