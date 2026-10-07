/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.biochemistry.util.toMoleculeCount
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable
import org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble

/**
 * This condition is valid if a selected biomolecule is present in the neighborhood of the node.
 *
 * @param molecule      the molecule to check
 * @param concentration the minimum concentration
 * @param reaction      the reaction owning this condition
 * @param environment   the environment
 */
class BiomolPresentInNeighbor(
    private val environment: Environment<Double, *>,
    reaction: NodeReaction<Double>,
    private val molecule: Biomolecule,
    private val concentration: Double,
) : AbstractNeighborCondition<Double>(environment, reaction) {

    private val requiredMolecules = concentration.toMoleculeCount(this)

    init {
        setUpObservability()
    }

    override fun observeNeighborWeight(neighbor: Node<Double>): Observable<Double> =
        neighbor.takeIf { it.asPropertyOrNull<Double, CellProperty<*>>() != null }?.let { n ->
            // the neighbor is eligible, its propensity is computed using the concentration of the biomolecule
            n.observeConcentration(molecule).map { maybeValue ->
                maybeValue.fold(
                    ifEmpty = { 0.0 },
                    ifSome = {
                        val availableMolecules = it.toMoleculeCount(this)
                        if (availableMolecules >= requiredMolecules) {
                            binomialCoefficientDouble(availableMolecules, requiredMolecules)
                        } else {
                            0.0
                        }
                    },
                )
            }
        } ?: observe(0.0)

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): BiomolPresentInNeighbor =
        BiomolPresentInNeighbor(environment, newReaction, molecule, concentration)

    override fun toString(): String = "$molecule >= $concentration in neighbor"

    private fun setUpObservability() {
        setValidity(
            observeValidNeighbors().map { validNeighbors ->
                val current = environment.getNeighborhood(targetNode).current
                validNeighbors
                    .takeIf { it.isNotEmpty() }?.entries
                    ?.filter { it.key.asPropertyOrNull<Double, CellProperty<*>>() != null }
                    ?.all { it.key in current && it.key.getConcentration(molecule) >= concentration }
                    ?: false
            },
        )
    }
}
