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
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.biochemistry.EnvironmentNode
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.Observable

/** A condition requiring [requiredQuantity] units of [biomolecule] in the surrounding environment. */
class BiomolPresentInEnv<P : Position<out P>>(
    private val environment: Environment<Double, P>,
    reaction: NodeReaction<Double>,
    private val biomolecule: Biomolecule,
    requiredQuantity: Double,
) : GenericMoleculePresent<Double>(reaction, biomolecule, requiredQuantity) {

    override val quantity: Observable<Double> = environment
        .getNeighborhood(targetNode)
        .switchMap { neighborhood ->
            neighborhood.neighbors
                .filterIsInstance<EnvironmentNode>()
                .map { neighbor -> neighbor.observeConcentration(biomolecule).map { it.getOrElse { 0.0 } } }
                .combineLatest { quantities -> quantities.sum() }
                .map { it.getOrElse { 0.0 } }
        }.mergeWith(environment.observeLayerValue(biomolecule, targetNode)) { neighboringQuantity, layerQuantity ->
            neighboringQuantity + (layerQuantity ?: 0.0)
        }

    init {
        setValidity(quantity.map { it >= requiredQuantity })
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): BiomolPresentInEnv<P> =
        BiomolPresentInEnv(environment, newReaction, biomolecule, requiredQuantity)
}
