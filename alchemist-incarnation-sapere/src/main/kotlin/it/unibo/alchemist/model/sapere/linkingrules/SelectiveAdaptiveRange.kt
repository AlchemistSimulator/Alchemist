/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.networks.AdaptiveRange
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule

/**
 * An [AdaptiveRange] that links only the nodes carrying an LSA molecule of the given type.
 *
 * @param T concentration type
 * @param P position type
 * @param environment the environment whose nodes this model links
 * @param radius the initial range of every node
 * @param desiredNeighborsCount the preferred number of neighbors
 * @param tolerance if the number of neighbors is smaller than [desiredNeighborsCount] - [tolerance], the range is
 * increased; if it is higher than [desiredNeighborsCount] + [tolerance], it is decreased
 * @param minRange the minimum range
 * @param maxRange the maximum range
 * @param adjustment the amount the range is changed by when out of tolerance
 * @param molType the molecule whose presence allows links to be created
 */
open class SelectiveAdaptiveRange<T, P : Position<P>>
@JvmOverloads
constructor(
    environment: Environment<T, P>,
    radius: Double,
    desiredNeighborsCount: Int,
    tolerance: Int,
    minRange: Double = DEFAULT_MINRANGE,
    maxRange: Double = DEFAULT_MAXRANGE,
    adjustment: Double = DEFAULT_ADJUSTMENT,
    molType: String = DEFAULT_MOLECULETYPE,
) : AdaptiveRange<T, P>(environment, radius, desiredNeighborsCount, tolerance, minRange, maxRange, adjustment) {

    private val moleculeType = LsaMolecule(molType)

    /**
     * Removes the neighbors not carrying the filter molecule, besides those the [AdaptiveRange] removes.
     */
    override fun conditionForRemoval(
        environment: Environment<T, P>,
        center: Node<T>,
        neighbor: Node<T>,
        centerRange: Double,
        neighRange: Double,
    ): Boolean = !neighbor.contains(moleculeType) ||
        super.conditionForRemoval(environment, center, neighbor, centerRange, neighRange)

    // Whether a node carries the filter molecule, which qualifies it as a neighbor.
    final override fun observeRemovalInputs(node: Node<T>): Observable<*> = node.containsMolecule(moleculeType)

    /**
     * Default parameters.
     */
    companion object {
        /**
         * The default filter molecule.
         */
        const val DEFAULT_MOLECULETYPE = "infrastructure"
    }
}
