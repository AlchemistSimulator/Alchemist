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
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.ObservableSets.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.combineLatestCollection
import it.unibo.alchemist.model.observation.AbstractObservable
import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.Observable
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Network model linking every enabled node to its [nodeCount] closest enabled nodes, and to the enabled nodes that
 * count it among their [nodeCount] closest; ties are broken by the lowest node id.
 * No node is linked while [environment] holds fewer than [expectedNodes] nodes.
 *
 * The closest nodes of each node whose neighborhood is observed are tracked: they are recomputed whenever a node
 * moves, appears, disappears, or changes its enablement within the distance of the farthest of them.
 * Neighborhoods account for the closest nodes of the nodes whose neighborhoods are observed, as the environment
 * does for all of its nodes.
 *
 * @param T concentration type
 * @param P position type
 */
open class ClosestN<T, P : Position<P>> @JvmOverloads constructor(
    protected val environment: Environment<T, P>,
    protected val nodeCount: Int,
    private val expectedNodes: Int = 0,
) : NetworkModel<T> {

    // For each node, the tracked nodes counting it among their closest ones.
    private val counting = HashMap<Node<T>, MutableSet<Node<T>>>()

    private val trackers = LinkedHashMap<Node<T>, Tracker>()

    init {
        require(nodeCount >= 1) { "The number of closest nodes must be positive, got $nodeCount" }
    }

    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> = TrackedNeighborhood(center)

    override fun toString(): String = "${javaClass.simpleName}($nodeCount)"

    /**
     * Whether [node] can be linked. This implementation always returns true; subclasses overriding it must also
     * override [canBeLinked].
     */
    protected open fun nodeIsEnabled(node: Node<T>): Boolean = true

    /**
     * Observes whether a node can be linked, consistently with [nodeIsEnabled].
     */
    protected open val Node<T>.canBeLinked get(): Observable<Boolean> = observe(true)

    /*
     * The closest nodes within a radius are the closest overall, once there are enough of them: the radius doubles
     * until it holds enough candidates, all the enabled nodes being the candidates otherwise.
     */
    private fun candidatesOf(node: Node<T>): List<Node<T>> = generateSequence(initialRadius()) { it * 2 }
        .take(MAX_RADIUS_DOUBLINGS)
        .takeIf { environment.nodes.current.size - 1 > nodeCount }
        ?.map { radius -> environment.nodesAround(node, radius).filter(::isCandidate) }
        ?.firstOrNull { it.size >= nodeCount }
        ?: environment.nodes.current.filter { it != node && isCandidate(it) }

    private fun closestOf(node: Node<T>): List<Node<T>> = when {
        !nodeIsEnabled(node) || environment.nodeCount.current < expectedNodes -> emptyList()
        else -> candidatesOf(node)
            .sortedWith(compareBy({ environment.distanceBetween(node, it) }, { it.id }))
            .take(nodeCount)
    }

    private fun isCandidate(node: Node<T>): Boolean = nodeIsEnabled(node)

    private fun counted(node: Node<T>): Neighborhood<T> = Neighborhood(
        node,
        (trackers[node]?.closest ?: closestOf(node)) + counting[node].orEmpty(),
    )

    /*
     * Twice the radius of a circle that, on average, holds the requested nodes, as the starting point of the search.
     * Degenerate environments, e.g., with all nodes aligned, make the estimate too small for spatial queries to be
     * precise: the search then starts from the largest side of the environment.
     */
    private fun initialRadius(): Double {
        val size = environment.sizeInDistanceUnits
        val largestSide = size.filter { it.isFinite() && it > 0 }.maxOrNull() ?: 1.0
        val area = size.fold(1.0) { area, side -> area * side }
        val estimate = 2 * sqrt(area / environment.nodes.current.size / PI * nodeCount)
        return estimate.takeIf { it.isFinite() && it >= largestSide * MIN_RELATIVE_RADIUS } ?: largestSide
    }

    private fun publish(node: Node<T>) {
        trackers[node]?.neighborhood?.publish()
    }

    /**
     * The neighborhood of [center], tracked while observed.
     */
    private inner class TrackedNeighborhood(private val center: Node<T>) : AbstractObservable<Neighborhood<T>>() {
        fun publish() = updateAndNotify(computeFresh())

        override fun computeFresh(): Neighborhood<T> = counted(center)

        override fun startMonitoring() = Tracker(center, this).let { tracker ->
            trackers[center] = tracker
            tracker.refresh()
        }

        override fun stopMonitoring() {
            trackers.remove(center)?.release()
        }
    }

    /**
     * Tracks the closest nodes of [node], publishing its [neighborhood] and those of the nodes it starts or stops
     * counting among its closest.
     */
    private inner class Tracker(val node: Node<T>, val neighborhood: TrackedNeighborhood) {
        var closest: List<Node<T>> = emptyList()
            private set

        private var radius: Double? = null

        private var trigger: Disposable? = null

        fun refresh() {
            val updated = closestOf(node)
            // The trigger must cover every node closer than the farthest of the closest ones.
            val required = updated.takeIf { it.size == nodeCount }
                ?.let { environment.distanceBetween(node, it.last()) * TOLERANCE }
                ?.takeIf { it > 0 }
            val current = radius
            val rearm = trigger == null ||
                (required == null) != (current == null) ||
                required != null && current != null && (required > current || required < current / 2)
            if (rearm) {
                trigger?.dispose()
                radius = required
                trigger = observeInputs(required).subscribe(invokeOnSubscription = false) { refresh() }
            }
            val previous = closest
            closest = updated
            val changed = (previous - updated.toSet()).onEach { stopCounting(it) } +
                (updated - previous.toSet()).onEach { counting.getOrPut(it) { LinkedHashSet() }.add(node) }
            neighborhood.publish()
            changed.forEach(::publish)
        }

        /*
         * The nodes this one counted among its closest are no longer linked to it. The nodes counting this one among
         * their closest replace it once it leaves the environment, as their triggers cover it.
         */
        fun release() {
            trigger?.dispose()
            closest.forEach { stopCounting(it) }
            closest.forEach(::publish)
        }

        // This node no longer counts closest among its closest ones; empty entries are dropped, not to retain nodes.
        private fun stopCounting(closest: Node<T>) {
            counting[closest]?.let { counters ->
                counters -= node
                if (counters.isEmpty()) {
                    counting -= closest
                }
            }
        }

        /*
         * The inputs of the closest nodes: those within the radius, or every node when the closest nodes are fewer
         * than requested, with their positions and enablement, along with the node itself.
         */
        private fun observeInputs(radius: Double?): Observable<*> {
            val describe = { other: Node<T> ->
                environment.positionOf(other).mergeWith(other.canBeLinked) { at, enabled -> at to enabled }
            }
            val others: Observable<*> = when (radius) {
                null -> environment.nodes.combineLatestCollection(describe) { it }
                else -> environment.observeNodesAround(node, radius).combineLatest(describe) { it.toList() }
            }
            val populated = environment.nodeCount.map { it >= expectedNodes }
            return others
                .mergeWith(describe(node)) { members, self -> members to self }
                .mergeWith(populated) { inputs, enough -> inputs to enough }
        }
    }

    private companion object {
        const val MAX_RADIUS_DOUBLINGS = 64
        const val MIN_RELATIVE_RADIUS = 1e-6
        const val TOLERANCE = 1.1
    }
}
