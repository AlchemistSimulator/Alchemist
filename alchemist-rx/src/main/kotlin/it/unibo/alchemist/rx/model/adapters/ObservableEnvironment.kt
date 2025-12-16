/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.adapters

import arrow.core.Option
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.rx.model.adapters.ObservableNeighborhood.Companion.asObservable
import it.unibo.alchemist.rx.model.adapters.ObservableNode.NodeExtension.asObservableNode
import it.unibo.alchemist.rx.model.adapters.RxIncarnation.Companion.asReactive
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableMap
import it.unibo.alchemist.rx.model.observation.ObservableMutableMap
import it.unibo.alchemist.rx.model.observation.ObservableMutableMap.ObservableMapExtensions.upsertValue
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet.Companion.toObservableSet
import it.unibo.alchemist.rx.model.observation.ObservableSet
import kotlin.collections.addAll

/**
 * An adapter for [Environment] to make it observable.
 */
class ObservableEnvironment<T, P : Position<out P>>(private val origin: Environment<T, P>) :
    Environment<T, P> by origin {

    private val observableNodes: MutableMap<Int, ObservableNode<T>> =
        origin.nodes.associate { it.id to it.asObservableNode() }.toMutableMap()

    private val observableNodesSet: ObservableMutableSet<ObservableNode<T>> =
        observableNodes.values.toSet().toObservableSet()

    private val observeNodePositions: ObservableMutableMap<Int, P> =
        ObservableMutableMap(origin.nodes.associate { it.id to getPosition(it) }.toMutableMap())

    private val observableNeighbourhood: ObservableMutableMap<Int, ObservableNeighborhood<T>> =
        ObservableMutableMap(
            origin.nodes.associate { it.id to getNeighborhood(it).asObservable() }.toMutableMap(),
        )

    /**
     * Holder for the reactive alternative of the associated [it.unibo.alchemist.model.Incarnation].
     */
    val rxIncarnation: RxIncarnation<T, P> = origin.incarnation.asReactive()

    /**
     * Return an observable view (as an [ObservableSet]) of the [ObservableNode]s contained
     * in this environment.
     *
     * @return an observable collection of the nodes inside this environment
     */
    fun observeNodes(): ObservableSet<ObservableNode<T>> = observableNodesSet

    /**
     * Yields an observable view of the given node.
     *
     * @param node the node to be observed
     * @return an [ObservableNode] instance of that node
     */
    fun observeNode(node: Node<T>): ObservableNode<T> = observableNodes.getOrPut(node.id) { node.asObservableNode() }

    /**
     * Yields an observable view of the given node's position in this environment.
     * @param node the node to observe
     * @return an [Observable] of [Option]s wrapping the input node's position. [none][arrow.core.none] if
     * the input node has no associated positions.
     */
    fun observeNodePosition(node: Node<T>): Observable<Option<P>> = observeNodePositions[node.id]

    /**
     * Observe the number of nodes in this environment.
     *
     * @return an [Observable] of integers representing the number of nodes contained in this environment.
     */
    fun observeNodeCounts(): Observable<Int> = observeNodePositions.map { it.keys.size }

    /**
     * @return a [ObservableMap] associating node ids to their positions.
     */
    fun observeAnyMovement(): ObservableMap<Int, P> = observeNodePositions

    /**
     * Yields the [neighborhood][ObservableNeighborhood] of the input [node] as an [Option],
     * [none][arrow.core.none] if the input node does not have an associated neighborhood.
     * The returned observable emits each time a change in this node's neighborhood is detected,
     * i.e. the input node is moved or some its members are removed/added to its neighborhood.
     *
     * @param node the center of the neighborhood to be observed.
     * @return an [Observable] of the input node's [neighborhood][ObservableNeighborhood].
     */
    fun observeNeighborhood(node: Node<T>): Observable<Option<ObservableNeighborhood<T>>> =
        observableNeighbourhood[node.id]

    override fun addNode(node: Node<T>, position: P): Boolean {
        val observableNode = node.asObservableNode()
        return origin.addNode(observableNode, position).also {
            if (it) {
                observableNodes[node.id] = observableNode
                observableNodesSet.add(observableNode)
                observeNodePositions[node.id] = position
                updateNeighborhood(observableNode)
            }
        }
    }

    override fun removeNode(node: Node<T>) {
        val oldNeighbourhood = getNeighborhood(node)
        origin.removeNode(node)
        observableNodes.remove(node.id)?.let(observableNodesSet::remove)
        updateNeighborhood(node, oldNeighbourhood, isAddition = false)
        observableNeighbourhood.remove(node.id)
        observeNodePositions.remove(node.id)
    }

    override fun moveNodeToPosition(node: Node<T>, newPosition: P) {
        val formerNeighborhood = getNeighborhood(node)
        origin.moveNodeToPosition(node, newPosition)
        val newPosition = getPosition(node).takeIf { it != observeNodePositions[node.id].current } ?: return
        observeNodePositions.upsertValue(node.id) { newPosition }
        updateNeighborhood(node, formerNeighborhood)
    }

    override fun getNodeByID(id: Int): ObservableNode<T> =
        observableNodes.getOrPut(id) { origin.getNodeByID(id).asObservableNode() }

    /**
     * Bulk update both former and new neighbors with current neighbourhood situation.
     * Not very efficient...
     */
    private fun updateNeighborhood(
        node: Node<T>,
        oldNeighbourhood: Neighborhood<T>? = null,
        isAddition: Boolean = true,
    ) {
        if (linkingRule.isLocallyConsistent) {
            buildSet {
                if (oldNeighbourhood != null) addAll(oldNeighbourhood)
                if (isAddition) {
                    addAll(getNeighborhood(node))
                    add(node)
                }
            }.map { neigh ->
                observableNeighbourhood.upsertValue(neigh.id) { getNeighborhood(neigh).asObservable() }
            }
        } else {
            // don't know exactly the entity of the update, let's stay safe
            // and refresh everything (since worst case is that every node gets updated :/)...
            nodes.forEach { n ->
                observableNeighbourhood.upsertValue(n.id) { getNeighborhood(n).asObservable() }
            }
        }
    }

    companion object {

        /**
         * Converts the current [Environment] into an [ObservableEnvironment].
         *
         * This allows the environment to be wrapped in an observable interface,
         * enabling the ability to monitor various aspects of the environment's state,
         * such as node positions, movements, and neighborhoods.
         *
         * @param T the type of the concentration values of the environment.
         * @param P the type of the positions in the environment, constrained to [Position].
         * @receiver the environment to be wrapped as an observable environment.
         * @return an [ObservableEnvironment] instance wrapping the current environment.
         */
        fun <T, P : Position<out P>> Environment<T, P>.asObservableEnvironment() = this as? ObservableEnvironment<T, P>
            ?: ObservableEnvironment(this)
    }
}
