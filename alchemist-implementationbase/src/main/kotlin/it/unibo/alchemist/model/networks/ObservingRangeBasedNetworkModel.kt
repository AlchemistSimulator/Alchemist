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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observables.util.ObservableSets.combineLatest
import it.unibo.alchemist.model.observation.Observable

/**
 * A [RangeBasedNetworkModel] whose links depend on the state of the nodes, such as their positions or contents:
 * each node exposes it as an [Observed], through [observeInput], and whether two nodes are linked is a function of
 * their inputs only. Neighborhoods are recomputed whenever the nodes within range change, or the input of the center
 * or of any node within range does.
 *
 * @param T concentration type
 * @param P position type
 * @param Observed the state of a node that links depend on
 * @param environment the environment whose nodes this model links
 * @param range the distance within which nodes may be linked
 */
abstract class ObservingRangeBasedNetworkModel<T, P : Position<P>, Observed>(
    environment: Environment<T, P>,
    range: Double,
) : RangeBasedNetworkModel<T, P>(environment, range) {

    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> =
        environment.observeNodesAround(center, range)
            .combineLatest({ node -> observeInput(node).map { node to it } }) { it.toList() }
            .mergeWith(observeInput(center)) { members, centerInput -> members to centerInput }
            .map { (members, centerInput) ->
                // Members may lag behind a movement in progress: distances are checked on current positions.
                Neighborhood(
                    center,
                    members
                        .filter { (node, input) ->
                            environment.distanceBetween(center, node) <= range &&
                                isLinked(center, centerInput, node, input)
                        }
                        .map { it.first },
                )
            }

    /**
     * Whether [center] is linked to [neighbor] given their current inputs.
     */
    final override fun isLinked(center: Node<T>, neighbor: Node<T>): Boolean =
        isLinked(center, observeInput(center).current, neighbor, observeInput(neighbor).current)

    /**
     * Whether [center], whose input is [centerInput], is linked to [neighbor], a node within range whose input is
     * [neighborInput].
     */
    protected abstract fun isLinked(
        center: Node<T>,
        centerInput: Observed,
        neighbor: Node<T>,
        neighborInput: Observed,
    ): Boolean

    /**
     * Observes the input of [node], namely the state of [node] that links depend on.
     */
    protected abstract fun observeInput(node: Node<T>): Observable<Observed>
}
