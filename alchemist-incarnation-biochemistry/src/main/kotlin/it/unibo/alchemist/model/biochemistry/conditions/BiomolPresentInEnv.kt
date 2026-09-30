/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import arrow.core.getOrElse
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.biochemistry.EnvironmentNode
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest
import it.unibo.alchemist.model.observation.ObservableExtensions.switchMap

/** A condition requiring [requiredQuantity] units of [biomolecule] in the surrounding environment. */
class BiomolPresentInEnv<P : Position<out P>>(
    private val environment: Environment<Double, P>,
    node: Node<Double>,
    private val biomolecule: Biomolecule,
    requiredQuantity: Double,
) : GenericMoleculePresent<Double>(node, biomolecule, requiredQuantity) {

    override val quantity: Observable<Double> = environment
        .getNeighborhood(node)
        .switchMap { neighborhood ->
            neighborhood.neighbors
                .filterIsInstance<EnvironmentNode>()
                .map { neighbor -> neighbor.observeConcentration(biomolecule).map { it.getOrElse { 0.0 } } }
                .combineLatest { quantities -> quantities.sum() }
                .map { it.getOrElse { 0.0 } }
        }.mergeWith(environment.getPosition(node)) { neighboringQuantity, position ->
            neighboringQuantity + (environment.getLayer(biomolecule)?.getValue(position) ?: 0.0)
        }

    init {
        setValidity(quantity.map { it >= requiredQuantity })
    }

    override fun cloneCondition(newNode: Node<Double>, newReaction: NodeReaction<Double>): BiomolPresentInEnv<P> =
        BiomolPresentInEnv(environment, newNode, biomolecule, requiredQuantity)
}
