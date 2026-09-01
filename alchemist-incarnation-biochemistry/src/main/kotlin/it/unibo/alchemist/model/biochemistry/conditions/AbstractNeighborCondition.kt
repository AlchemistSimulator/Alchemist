/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import arrow.core.getOrElse
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest
import it.unibo.alchemist.model.observation.ObservableExtensions.switchMap

/** A condition satisfied when at least one neighbor has a positive reaction-specific selection weight. */
abstract class AbstractNeighborCondition<T>(private val environment: Environment<T, *>, node: Node<T>) :
    AbstractCondition<T>(node) {

    private val validNeighbors: Observable<Map<Node<T>, Double>> = environment
        .getNeighborhood(node)
        .switchMap { neighborhood ->
            neighborhood.neighbors
                .map { neighbor -> observeNeighborWeight(neighbor).map { neighbor to it } }
                .combineLatest { weights -> weights.filter { (_, weight) -> weight > 0.0 }.toMap() }
                .map { it.getOrElse(::emptyMap) }
        }

    init {
        addObservableDependency(validNeighbors)
    }

    abstract override fun cloneCondition(newNode: Node<T>, newReaction: NodeReaction<T>): AbstractNeighborCondition<T>

    protected fun getEnvironment(): Environment<T, *> = environment

    /** Current eligible neighbors and their selection weights. */
    fun getValidNeighbors(): Map<Node<T>, Double> = validNeighbors.current

    /** Observable eligible neighbors and their selection weights. */
    fun observeValidNeighbors(): Observable<Map<Node<T>, Double>> = validNeighbors

    /**
     * Observes the weight with which [neighbor] participates in neighbor selection.
     *
     * A non-positive value makes the neighbor ineligible.
     */
    protected abstract fun observeNeighborWeight(neighbor: Node<T>): Observable<Double>
}
