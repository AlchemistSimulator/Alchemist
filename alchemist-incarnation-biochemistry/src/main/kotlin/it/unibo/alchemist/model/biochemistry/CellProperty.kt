/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry

import arrow.core.getOrElse
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.Observable.ObservableExtensions.currentOrNull
import it.unibo.alchemist.model.observation.ObservableMutableMap
import it.unibo.alchemist.model.observation.ObservableMutableMap.ObservableMapExtensions.upsertValue

/**
 * A node's capability to behave as a cell.
 */
interface CellProperty<P : Vector<P>> : NodeProperty<Double> {

    /**
     * The map junction - node - quantity.
     */
    val junctions: ObservableMutableMap<Junction, ObservableMutableMap<Node<Double>, Int>>

    /**
     * Add a junction to the current node.
     * [junction] the junction.
     * [neighbor] the neighbor node at the other side of the junction.
     */
    fun addJunction(junction: Junction, neighbor: Node<Double>) {
        junctions.upsertValue(junction) { oldValue ->
            oldValue?.apply {
                upsertValue(neighbor) {
                    when {
                        it != null -> it + 1
                        else -> 1
                    }
                }
            } ?: ObservableMutableMap(mutableMapOf(neighbor to 1))
        }
    }

    /**
     * Return true if a junction is present in the current node, false otherwise.
     * Note: a junction is considered present if the method junction.equals(j) return true.
     * @param junction the junction.
     */
    fun containsJunction(junction: Junction): Boolean = observeContainsJunction(junction).current

    /**
     * Return an observable view of the presence of the [junction] in the current node.
     *
     * @param junction the junction.
     */
    fun observeContainsJunction(junction: Junction): Observable<Boolean> = junctions[junction].map { it.isSome() }

    /**
     * Removes a [junction] from this [node].
     *
     * [neighbor] the node at the other side of the junction.
     */
    fun removeJunction(junction: Junction, neighbor: Node<Double>) {
        if (!containsJunction(junction)) {
            return
        }

        val inner = junctions[junction].currentOrNull()
        inner?.let {
            when (it[neighbor].current.getOrElse { 0 }) {
                1 -> it.remove(neighbor)
                else -> it[neighbor].currentOrNull()?.minus(1)
            }
            if (it.isEmpty()) {
                junctions.remove(junction)
            } else {
                junctions[junction] = inner
            }
            junction.moleculesInCurrentNode.forEach { (biomolecule, value) ->
                with(node) {
                    setConcentration(biomolecule, getConcentration(biomolecule) + value)
                }
            }
        }
    }

    /**
     * Returns a set of [Node]s which are linked with the current node by a junction of the type [junction].
     */
    fun getNeighborLinkWithJunction(junction: Junction): Set<Node<Double>> =
        observeNeighborLinkWithJunction(junction).current

    /**
     * Returns an observable view of the set of [Node]s which are linked with the current
     * node by a [junction].
     *
     * @param junction the junction to check
     * @return an [Observable] set of the node linked with current node.
     */
    fun observeNeighborLinkWithJunction(junction: Junction): Observable<Set<Node<Double>>> =
        junctions[junction].map { maybeMap -> maybeMap.map { it.current.keys }.getOrElse { LinkedHashSet() } }

    /**
     * Returns set of [Node]s which are linked by a junction with the current [node].
     */
    fun getAllNodesLinkWithJunction(): Set<Node<Double>> = junctions.current.values.flatMap { it.current.keys }.toSet()

    /**
     * The total number of junctions in this [node].
     */
    val junctionsCount: Int
        get() = junctions.current.values.flatMap { it.current.values }.sum()

    /**
     * The polarization versor, e.g. a versor indicating the direction in which the cell will move the next time.
     */
    var polarizationVersor: P

    /**
     * Add [versor] to the polarization versor inside the cell; useful for considering the combination of various
     * stimuli in a cell.
     */
    fun addPolarizationVersor(versor: P)
}
