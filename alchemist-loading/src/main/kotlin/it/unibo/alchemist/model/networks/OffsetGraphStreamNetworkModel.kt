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
import it.unibo.alchemist.model.observables.ObservableMutableMap
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.Observable
import kotlin.streams.toList
import org.graphstream.graph.Graph

/**
 * A [NetworkModel] that statically connects the nodes of [environment] as they were configured by GraphStream.
 * An [offset] is used to determine the id of the environment's nodes when compared to the one of the
 * provided [graph].
 *
 * The neighbors are linked as soon as they are part of the environment: each neighborhood follows only the presence of
 * the graph neighbors of its center, so that a node joining or leaving notifies only the neighborhoods it belongs to.
 */
class OffsetGraphStreamNetworkModel<T, P : Position<P>>(
    private val environment: Environment<T, P>,
    val offset: Int,
    val graph: Graph,
) : NetworkModel<T> {

    // The nodes of the environment by id, kept up to date while any neighborhood is observed.
    private val present = ObservableMutableMap<Int, Node<T>>()

    private var presenceTracking: Disposable? = null

    private var observedNeighborhoods = 0

    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> = GraphNeighborhood(center)

    override fun toString(): String = "${javaClass.simpleName}($offset)"

    private fun graphNeighborIdsOf(center: Node<T>): Set<Int> {
        val actualId = center.id - offset
        val graphNode = if (actualId >= 0 && actualId < graph.nodeCount) graph.getNode(actualId) else null
        return graphNode
            ?.neighborNodes()
            ?.mapToInt { it.index + offset }
            ?.toList()
            .orEmpty()
            .toSet()
    }

    /*
     * The neighborhood of center, following the presence of its graph neighbors while observed. The presence of all
     * the nodes is tracked once for every neighborhood, from the first one being observed to the last one left.
     */
    private inner class GraphNeighborhood(private val center: Node<T>) : Observable<Neighborhood<T>> {

        private val neighborIds = graphNeighborIdsOf(center)

        private val base: Observable<Neighborhood<T>> = neighborIds.map { present[it] }
            .combineLatest { presence -> presence.mapNotNull { it.getOrNull() } }
            .map { neighbors -> Neighborhood(center, neighbors.getOrNull().orEmpty()) }

        override val current: Neighborhood<T> get() = when {
            isObserved -> base.current
            else -> Neighborhood(center, environment.nodes.current.filter { it.id in neighborIds })
        }

        override val observers: List<Any> get() = base.observers

        override val observingCallbacks: Map<Any, List<(Neighborhood<T>) -> Unit>> get() = base.observingCallbacks

        private val isObserved: Boolean get() = observers.isNotEmpty()

        override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Neighborhood<T>) -> Unit) {
            if (!isObserved && observedNeighborhoods++ == 0) {
                // The presence is tracked before the neighborhood reads it.
                presenceTracking = environment.nodes.subscribe { nodes ->
                    present.clearAndPutAll(nodes.associateBy(Node<T>::id))
                }
            }
            base.onChange(registrant, invokeOnRegistration, callback)
        }

        override fun stopWatching(registrant: Any) {
            val wasObserved = isObserved
            base.stopWatching(registrant)
            if (wasObserved && !isObserved && --observedNeighborhoods == 0) {
                presenceTracking?.dispose()
                presenceTracking = null
                present.clearAndPutAll(emptyMap())
            }
        }

        override fun toString(): String = "GraphNeighborhood($current)"
    }
}
