/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import arrow.core.getOrElse
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest
import it.unibo.alchemist.model.observation.ObservableExtensions.switchMap

/**
 * A condition that evaluates whether at least one neighbor of a node has the specified [concentration]
 * of a given [molecule][target].
 *
 * @param node the node for which the condition is being evaluated.
 * @param environment the environment containing the node and its neighborhood.
 * @param target the molecule to check in the neighborhood
 * @param concentration the target concentration of the molecule to be checked in the neighborhood
 * @param T the type of concentration
 */
class NeighborHasConcentration<T>(
    node: Node<T>,
    val environment: Environment<T, *>,
    val target: Molecule,
    val concentration: T,
) : AbstractCondition<T>(node) {

    init {
        setValidity(
            environment.observeNeighborhood(node).switchMap { neighborhood ->
                neighborhood.neighbors.map { it.observeConcentration(target) }
                    .combineLatest { neighborsConcentrations ->
                        neighborsConcentrations.any { it.isSome { nConc -> nConc == concentration } }
                    }.map { it.getOrElse { false } }
            },
        )
    }

    override fun getContext(): Context = Context.NEIGHBORHOOD

    override fun cloneCondition(newNode: Node<T>, newReaction: Reaction<T>): Condition<T> =
        NeighborHasConcentration(newNode, environment, target, concentration)
}
