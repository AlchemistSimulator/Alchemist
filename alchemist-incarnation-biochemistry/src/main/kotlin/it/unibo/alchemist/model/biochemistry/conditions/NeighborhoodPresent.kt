/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable

/**
 * A condition valid when the node has at least one neighboring cell, namely a neighbor with a [CellProperty].
 *
 * @param T the concentration type
 */
class NeighborhoodPresent<T>(private val environment: Environment<T, *>, reaction: NodeReaction<T>) :
    AbstractNeighborCondition<T>(environment, reaction) {
    init {
        setValidity(environment.getNeighborhood(targetNode).map { it.neighbors.any(::isCell) })
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): NeighborhoodPresent<T> =
        NeighborhoodPresent(environment, newReaction)

    /**
     * Every neighboring cell is eligible with the same weight.
     * Properties are not observable yet, so they are assumed to be static.
     */
    override fun observeNeighborWeight(neighbor: Node<T>): Observable<Double> =
        observe(if (isCell(neighbor)) 1.0 else 0.0)

    override fun toString(): String = "$targetNode has at least one neighboring cell"

    private fun isCell(node: Node<T>): Boolean = node.properties.any { it is CellProperty<*> }
}
