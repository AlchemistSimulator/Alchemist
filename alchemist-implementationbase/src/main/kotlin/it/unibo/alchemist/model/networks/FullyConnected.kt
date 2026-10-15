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
 * This model connects each and every node of [environment] to each and every other.
 */
class FullyConnected<T, P : Position<P>>(private val environment: Environment<T, P>) : NetworkModel<T> {
    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> = environment.nodes.map { nodes ->
        Neighborhood(center, nodes.filter { it != center })
    }

    override fun toString(): String = javaClass.simpleName
}
