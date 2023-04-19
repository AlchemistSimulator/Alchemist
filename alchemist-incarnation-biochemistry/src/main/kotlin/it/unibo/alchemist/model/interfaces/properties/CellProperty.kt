/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.implementations.molecules.Junction
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.NodeProperty
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A node's capability to behave as a cell.
 */
interface CellProperty<P : Vector<P>> : NodeProperty<Double> {

    /**
     * The map junction - node - quantity.
     */
    val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>>

    /**
     * Add a junction to the current node.
     * [junction] the junction.
     * [neighbor] the neighbor node at the other side of the junction.
     */
    fun addJunction(junction: Junction, neighbor: Node<Double>) {
        if (containsJunction(junction)) {
            junctions[junction]?.let {
                if (it.containsKey(neighbor)) {
                    it[neighbor] = it[neighbor]?.plus(1) ?: 1
                } else {
                    it[neighbor] = 1
                }
                junctions[junction] = it
            }
        } else {
            val tmp: MutableMap<Node<Double>, Int> = LinkedHashMap(1)
            tmp[neighbor] = 1
            junctions[junction] = tmp
        }
    }

    /**
     * Return true if a junction is present in the current node, false otherwise.
     * Note: a junction is considered present if the method junction.equals(j) return true.
     * [junction] the junction.
     */
    fun containsJunction(junction: Junction) = junctions.containsKey(junction)

    /**
     * Removes a [junction] from this [node].
     *
     * [neighbor] the node at the other side of the junction.
     */
    fun removeJunction(junction: Junction, neighbor: Node<Double>) {
        if (containsJunction(junction)) {
            val inner: MutableMap<Node<Double>, Int>? = junctions[junction]
            inner?.let {
                when (it[neighbor]) {
                    1 -> it.remove(neighbor)
                    else -> it[neighbor]?.minus(1)
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
    }

    /**
     * Returns a set of [Node]s which are linked with the current node by a junction of the type [junction].
     */
    fun getNeighborLinkWithJunction(junction: Junction): Set<Node<Double>> {
        return junctions.getOrElse(junction) { LinkedHashMap() }.keys
    }

    /**
     * Returns set of [Node]s which are linked by a junction with the current [node].
     */
    fun getAllNodesLinkWithJunction(): Set<Node<Double>> {
        return junctions.values.flatMap { it.keys }.toSet()
    }

    /**
     * The total number of junctions presents in this [node].
     */
    val junctionsCount: Int
        get() = junctions.values.flatMap { it.values }.sum()

    /**
     * The polarization versor, e.g. a versor indicating the direction in which the cell will move the next time.
     */
    var polarizationVersor: P

    /**
     * add [versor] to the polarization versor inside the cell; useful for considering the combination of various
     * stimuli in a cell.
     */
    fun addPolarizationVersor(versor: P)
}
