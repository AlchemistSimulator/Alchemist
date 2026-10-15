/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import arrow.core.getOrElse
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observation.Observable

/**
 * A [ClosestN] linking only the nodes whose concentration of [molecule] is [value].
 *
 * @param T concentration type
 * @param P position type
 */
class ConditionalClosestN<T, P : Position<P>>(
    environment: Environment<T, P>,
    nodeCount: Int,
    expectedNodes: Int,
    private val molecule: Molecule,
    private val value: T,
) : ClosestN<T, P>(environment, nodeCount, expectedNodes) {

    /**
     * Builds a [ConditionalClosestN] with no expected number of nodes.
     */
    constructor(environment: Environment<T, P>, nodeCount: Int, molecule: Molecule, value: T) :
        this(environment, nodeCount, 0, molecule, value)

    override fun nodeIsEnabled(node: Node<T>): Boolean = node[molecule].map { it == value }.getOrElse { false }

    override val Node<T>.canBeLinked: Observable<Boolean> get() =
        observeConcentration(molecule).map { concentration ->
            concentration.map { it == value }.getOrElse { false }
        }
}
