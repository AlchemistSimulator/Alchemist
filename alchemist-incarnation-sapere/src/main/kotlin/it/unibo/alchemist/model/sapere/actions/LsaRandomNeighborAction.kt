/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import kotlin.math.abs
import kotlin.math.max
import org.apache.commons.math3.random.RandomGenerator

/**
 * Adds the LSA [molecule] to a neighbor: the one selected by a previous neighborhood condition, if any,
 * otherwise a random valid neighbor drawn using [randomGenerator]. Example: `+<id, X, n>`.
 */
open class LsaRandomNeighborAction(
    /**
     * The random engine, required only when a random neighbor must be drawn.
     */
    protected val randomGenerator: RandomGenerator?,
    /**
     * The current environment.
     */
    protected val environment: Environment<List<ILsaMolecule>, *>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    molecule: ILsaMolecule,
) : LsaStandardAction(randomGenerator, reaction, molecule) {
    private val initO: Boolean
    private val initD: Boolean
    private val initNeigh: Boolean
    private val initRoute: Boolean
    private val mapEnvironment: MapEnvironment<List<ILsaMolecule>, *, *>? =
        environment as? MapEnvironment<List<ILsaMolecule>, *, *>

    init {
        val moleculeString = molecule.toString()
        initO = moleculeString.contains(LsaMolecule.SYN_O)
        initD = moleculeString.contains(LsaMolecule.SYN_D)
        initNeigh = moleculeString.contains(LsaMolecule.SYN_NEIGH)
        initRoute = moleculeString.contains(LsaMolecule.SYN_ROUTE)
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): LsaRandomNeighborAction =
        LsaRandomNeighborAction(randomGenerator, environment, newReaction, molecule)

    override fun execute() {
        val nodes = checkNotNull(nodes)
        if (nodes.isNotEmpty()) {
            val selectedNodeId = checkNotNull(matches)[LsaMolecule.SYN_SELECTED]
            if (selectedNodeId == null) {
                // Choose a random neighbor among those valid
                val target = nodes[abs(checkNotNull(randomGenerator).nextInt() % nodes.size)]
                setSynthectics(target)
                setConcentration(target)
            } else {
                // There was an operation that fixed a single neighbor
                val id = (selectedNodeId.data as Double).toInt()
                val target = checkNotNull(nodes.firstOrNull { it.id == id }) {
                    "there is probably a bug in ${javaClass.name}\nMatches: $matches\nNodes: $nodes"
                }
                setSynthectics(target)
                setConcentration(target)
            }
        }
    }

    /**
     * Sets the synthetic variables, using [target] as reference (e.g., for computing the distance).
     */
    protected open fun setSynthectics(target: ILsaNode) {
        // #D and #ROUTE
        var distance = if (initD || initRoute) computeDistance(target) else Double.NaN
        if (initD) {
            distance = computeDistance(target)
            setSyntheticD(distance)
        }
        if (initRoute) {
            mapEnvironment?.computeRoute(targetNode, target)?.let { route ->
                distance = max(distance, route.length())
            }
            setSyntheticRoute(distance)
        }
        // #NEIGH
        if (initNeigh) {
            setSyntheticNeigh(environment.getNeighborhood(target).current.neighbors)
        }
        // #O
        if (initO) {
            setSyntheticO()
        }
    }

    private fun computeDistance(target: ILsaNode): Double = environment.getDistanceBetweenNodes(targetNode, target)

    override fun toString(): String = "+$molecule"
}
