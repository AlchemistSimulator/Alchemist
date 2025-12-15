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
import kotlin.collections.addAll

class ObservableEnvironment<T, P : Position<out P>>(private val origin: Environment<T, P>) :
    Environment<T, P> by origin {

    private val observableNodes: MutableMap<Int, ObservableNode<T>> =
        origin.nodes.associate { it.id to it.asObservableNode() }.toMutableMap()

    private val observeNodePositions: ObservableMutableMap<Int, P> =
        ObservableMutableMap(origin.nodes.associate { it.id to getPosition(it) }.toMutableMap())

    private val observableNeighbourhood: ObservableMutableMap<Int, ObservableNeighborhood<T>> =
        ObservableMutableMap(
            origin.nodes.associate { it.id to getNeighborhood(it).asObservable() }.toMutableMap(),
        )

    val rxIncarnation: RxIncarnation<T, P> = origin.incarnation.asReactive()

    fun observeNode(node: Node<T>): ObservableNode<T> = observableNodes.getOrPut(node.id) { node.asObservableNode() }

    fun observeNodePosition(node: Node<T>): Observable<Option<P>> = observeNodePositions[node.id]

    fun observeNodeCounts(): Observable<Int> = observeNodePositions.map { it.keys.size }

    fun observeAnyMovement(): ObservableMap<Int, P> = observeNodePositions

    fun observeNeighborhood(node: Node<T>): Observable<Option<ObservableNeighborhood<T>>> =
        observableNeighbourhood[node.id]

    override fun addNode(node: Node<T>, position: P): Boolean {
        val observableNode = node.asObservableNode()
        return origin.addNode(observableNode, position).also {
            if (it) {
                observableNodes[node.id] = observableNode
                observeNodePositions[node.id] = position
                updateNeighborhood(observableNode)
            }
        }
    }

    override fun removeNode(node: Node<T>) {
        val oldNeighbourhood = getNeighborhood(node)
        origin.removeNode(node)
        observableNodes.remove(node.id)
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
        fun <T, P : Position<P>> Environment<T, P>.asObservableEnvironment() = ObservableEnvironment(this)
    }
}
