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
import it.unibo.alchemist.model.observables.ObservableMutableMap
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable
import kotlin.math.max
import kotlin.math.min

/**
 * A [RangeBasedNetworkModel] where each node has its own range, adjusted to keep its number of neighbors close to a
 * target: two nodes are linked when each is within the range of the other, and [conditionForRemoval] does not hold.
 * Whenever its neighborhood changes, the range of a node converges, by steps of [adjustment] within [minRange] and
 * [maxRange], until its number of neighbors is within [tolerance] of [desiredNeighbors].
 *
 * @param T concentration type
 * @param P position type
 * @param environment the environment whose nodes this model links
 * @param radius the initial range of every node
 * @param desiredNeighbors the preferred number of neighbors
 * @param tolerance if the number of neighbors is smaller than [desiredNeighbors] - [tolerance], the range is
 * increased; if it is higher than [desiredNeighbors] + [tolerance], it is decreased
 * @param minRange the minimum range
 * @param maxRange the maximum range
 * @param adjustment the amount the range is changed by when out of tolerance
 */
open class AdaptiveRange<T, P : Position<P>>
@JvmOverloads
@Suppress("LongParameterList") // The parameters of the model, as loaded from simulation descriptors.
constructor(
    environment: Environment<T, P>,
    private val radius: Double,
    desiredNeighbors: Int,
    private val tolerance: Int,
    private val minRange: Double = DEFAULT_MINRANGE,
    private val maxRange: Double = DEFAULT_MAXRANGE,
    private val adjustment: Double = DEFAULT_ADJUSTMENT,
) : ObservingRangeBasedNetworkModel<T, P, AdaptiveRange.LinkState<P>>(
    environment,
    // The candidates are the nodes within the maximum range, which no range ever exceeds.
    maxRange,
) {

    // How many range adjustments are being propagated, each one triggered by the previous.
    private var cascadingAdjustments = 0

    private val ranges = ObservableMutableMap<Int, Double>()

    private val targetNeighborCount = max(desiredNeighbors, 0)

    final override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> =
        super.neighborhoodOf(center).map { neighborhood -> neighborhood.also { converge(center, it.size()) } }

    /**
     * Acts as a filter: whether [neighbor], a node within the range of [center], must not be linked to it.
     * This implementation checks whether the distance between the nodes exceeds the range of the neighbor.
     *
     * @param environment the current environment
     * @param center the current node
     * @param neighbor the neighbor to test
     * @param centerRange the range of the current node
     * @param neighRange the range of the neighbor
     * @return true if the node must be removed, false otherwise
     */
    protected open fun conditionForRemoval(
        environment: Environment<T, P>,
        center: Node<T>,
        neighbor: Node<T>,
        centerRange: Double,
        neighRange: Double,
    ): Boolean = environment.distanceBetween(center, neighbor) > neighRange

    final override fun isLinked(
        center: Node<T>,
        centerInput: LinkState<P>,
        neighbor: Node<T>,
        neighborInput: LinkState<P>,
    ): Boolean = centerInput.position.distanceTo(neighborInput.position) <= centerInput.range &&
        !conditionForRemoval(environment, center, neighbor, centerInput.range, neighborInput.range)

    // Links depend on the positions and ranges of both nodes, and on what the removal condition observes.
    final override fun observeInput(node: Node<T>): Observable<LinkState<P>> = environment.positionOf(node)
        .mergeWith(ranges[node.id]) { position, _ -> position }
        .mergeWith(observeRemovalInputs(node)) { position, removalInput ->
            LinkState(position, rangeOf(node), removalInput)
        }

    /**
     * Observes the state, other than positions and ranges, that [conditionForRemoval] reads for [node] as a
     * neighbor. This implementation observes nothing; subclasses overriding [conditionForRemoval] must override it
     * accordingly.
     */
    protected open fun observeRemovalInputs(node: Node<T>): Observable<*> = observe(Unit)

    /*
     * Steps the range of the center until its neighbor count is within tolerance or the range hits its bounds, then
     * publishes it: changing a range updates the neighborhoods depending on it, which may adjust further ranges, so
     * the adjustments triggered by one another are capped, to bound mutually oscillating ranges.
     */
    private fun converge(center: Node<T>, neighborCount: Int) {
        if (cascadingAdjustments < MAX_CASCADING_ADJUSTMENTS && !withinTolerance(neighborCount)) {
            val initial = rangeOf(center)
            val candidates = environment.nodesAround(center, maxRange)
            var range = initial
            var count = neighborCount
            while (!withinTolerance(count)) {
                val next = when {
                    count > targetNeighborCount + tolerance -> max(range - adjustment, minRange)
                    else -> min(range + adjustment, maxRange)
                }
                if (next.compareTo(range) == 0) {
                    break
                }
                range = next
                count = candidates.count { linkedWithin(center, it, next) }
            }
            if (range.compareTo(initial) != 0) {
                cascadingAdjustments++
                try {
                    ranges[center.id] = range
                } finally {
                    cascadingAdjustments--
                }
            }
        }
    }

    private fun linkedWithin(center: Node<T>, neighbor: Node<T>, centerRange: Double): Boolean =
        environment.distanceBetween(center, neighbor) <= centerRange &&
            !conditionForRemoval(environment, center, neighbor, centerRange, rangeOf(neighbor))

    private fun rangeOf(node: Node<T>): Double = ranges.current[node.id] ?: radius

    private fun withinTolerance(neighborCount: Int): Boolean =
        neighborCount in max(1, targetNeighborCount - tolerance)..targetNeighborCount + tolerance

    /**
     * The state of a node that its links depend on: its [position], its current [range], and the [removalInput] that
     * [conditionForRemoval] reads, as observed by [observeRemovalInputs].
     */
    data class LinkState<P>(val position: P, val range: Double, val removalInput: Any?)

    /**
     * Default parameters.
     */
    companion object {
        /**
         * Default adjustment.
         */
        const val DEFAULT_ADJUSTMENT = 1.0

        /**
         * Default maximum range.
         */
        const val DEFAULT_MAXRANGE = 100.0

        /**
         * Default minimum range.
         */
        const val DEFAULT_MINRANGE = 1.0

        // At most this many range adjustments triggering one another are applied.
        private const val MAX_CASCADING_ADJUSTMENTS = 1000
    }
}
