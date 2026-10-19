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
import it.unibo.alchemist.model.observables.AbstractObservable
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.MutableObservable
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
    // No range ever exceeds the maximum one.
    maxRange,
) {

    /*
     * The range of each node by id, kept as long as the node exists, whether or not its neighborhood is observed.
     * The ranges of the nodes no longer in the environment are dropped when a new range would make them twice as many
     * as the nodes in it, so that dropping them costs a constant amortized time per range.
     */
    private val ranges = mutableMapOf<Int, MutableObservable<Double>>()

    private val targetNeighborCount = max(desiredNeighbors, 0)

    // The neighbor counts within tolerance of the desired one.
    private val tolerated = max(1, targetNeighborCount - tolerance)..targetNeighborCount + tolerance

    final override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> = ConvergingNeighborhood(center)

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

    // Links require neighbors within the range of the center: the query follows it, rather than the maximum range.
    final override fun observeCandidates(center: Node<T>): Observable<out Set<Node<T>>> =
        center.range.switchMap { environment.observeNodesAround(center, it) }

    // Links depend on the positions and ranges of both nodes, and on what the removal condition observes.
    final override fun observeInput(node: Node<T>): Observable<LinkState<P>> = environment.positionOf(node)
        .mergeWith(node.range) { position, range -> position to range }
        .mergeWith(observeRemovalInputs(node)) { (position, range), removalInput ->
            LinkState(position, range, removalInput)
        }

    /**
     * Observes the state, other than positions and ranges, that [conditionForRemoval] reads for [node] as a
     * neighbor. This implementation observes nothing; subclasses overriding [conditionForRemoval] must override it
     * accordingly.
     */
    protected open fun observeRemovalInputs(node: Node<T>): Observable<*> = observe(Unit)

    /*
     * Steps the range of the center, in the direction that brings its neighbor count towards the tolerated ones,
     * until the count is tolerated, it overshoots the tolerance, from too few neighbors to too many or vice versa,
     * or the range hits its bounds. Of the last two ranges, the one whose neighbor count is closest to the tolerated
     * ones is kept, the larger one if both are equally close. The range is published only if it changed: changing it
     * updates the neighborhoods depending on it, which may adjust further ranges.
     */
    private fun converge(center: Node<T>, currentNeighborCount: Int) {
        if (currentNeighborCount !in tolerated) {
            val growing = currentNeighborCount < tolerated.first

            // Whether the count still requires moving the range in the same direction.
            fun Int.needsAdjustment() = if (growing) this < tolerated.first else this > tolerated.last
            val initialRange = center.currentRange
            /*
             * The candidate neighbors with their distance, queried lazily. Shrinking ranges query once, at their
             * largest step; growing ranges query again whenever a step exceeds the queried range, doubling it up to
             * the maximum range, so that the queries cost at most a constant factor more than querying the final range.
             */
            var queriedRange = initialRange
            var candidates: List<Pair<Node<T>, Double>>? = null
            fun neighborsWithin(range: Double): Int {
                val known = candidates?.takeIf { range <= queriedRange }
                    ?: run {
                        queriedRange = if (growing) max(range, min(queriedRange * 2, maxRange)) else range
                        environment.nodesAround(center, queriedRange)
                            .map { it to environment.distanceBetween(center, it) }
                            .also { candidates = it }
                    }
                return known.count { (node, distance) ->
                    distance <= range && !conditionForRemoval(environment, center, node, range, node.currentRange)
                }
            }
            var lastRange = initialRange
            var lastNeighborCount = currentNeighborCount
            var nextRange = initialRange
            var nextNeighborCount = currentNeighborCount
            while (nextNeighborCount.needsAdjustment() && if (growing) nextRange < maxRange else nextRange > minRange) {
                lastRange = nextRange
                lastNeighborCount = nextNeighborCount
                nextRange = (nextRange + if (growing) adjustment else -adjustment).coerceIn(minRange, maxRange)
                nextNeighborCount = neighborsWithin(nextRange)
            }
            val lastDistance = lastNeighborCount.distanceFromTolerated()
            val nextDistance = nextNeighborCount.distanceFromTolerated()
            val converged = when {
                lastDistance == nextDistance -> max(lastRange, nextRange)
                lastDistance < nextDistance -> lastRange
                else -> nextRange
            }
            if (converged != initialRange) {
                center.range.current = converged
            }
        }
    }

    // How far the count is from the tolerated neighbor counts.
    private fun Int.distanceFromTolerated(): Int = when {
        this < tolerated.first -> tolerated.first - this
        this > tolerated.last -> this - tolerated.last
        else -> 0
    }

    private val Node<T>.currentRange: Double get() = range.current

    private val Node<T>.range: MutableObservable<Double> get() = ranges.getOrPut(id) {
        // if there are twice more nodes in tracking than there are in the environment, cleanup the mess
        val currentNodes = environment.nodes.current
        if (ranges.size >= 2 * currentNodes.size) {
            val present = currentNodes.mapTo(HashSet(currentNodes.size)) { it.id }
            ranges.keys.retainAll(present)
        }
        observe(radius)
    }

    /*
     * The neighborhood of center, whose range converges once each change of the neighborhood has been delivered to its
     * observers, so that the newer neighborhoods caused by an adjustment reach them after the one that triggered it,
     * rather than being overwritten by it. Reading the current neighborhood changes nothing.
     */
    private inner class ConvergingNeighborhood(private val center: Node<T>) : AbstractObservable<Neighborhood<T>>() {
        private val base = super@AdaptiveRange.neighborhoodOf(center)

        override fun computeFresh(): Neighborhood<T> = base.current

        override fun startObservingSources() {
            observeSource(base) { neighborhood ->
                updateAndNotify(neighborhood)
                // Delivering it may have adjusted ranges already: the latest neighborhood is the one to converge on.
                converge(center, base.current.neighbors.size)
            }
            converge(center, base.current.neighbors.size)
        }

        override fun toString(): String = "ConvergingNeighborhood($current)[from: $base]"
    }

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
    }
}
