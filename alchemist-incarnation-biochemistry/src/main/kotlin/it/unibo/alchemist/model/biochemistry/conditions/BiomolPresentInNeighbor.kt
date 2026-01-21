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
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.model.observation.Observable
import java.io.Serial
import org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble
import org.apache.commons.math3.util.FastMath

/**
 * This condition is valid if a selected biomolecule is present in the neighborhood of the node.
 *
 * @param molecule      the molecule to check
 * @param concentration the minimum concentration
 * @param node          the local node
 * @param environment   the environment
 */
class BiomolPresentInNeighbor(
    environment: Environment<Double, *>,
    node: Node<Double>,
    private val molecule: Biomolecule,
    private val concentration: Double,
) : AbstractNeighborCondition<Double>(environment, node) {

    init {
        declareDependencyOn(molecule)
        setUpObservability()
    }

    protected override fun observeNeighborPropensity(neighbor: Node<Double>): Observable<Double> =
        neighbor.takeIf { it.asPropertyOrNull<Double, CellProperty<*>>() != null }?.let { n ->
            // the neighbor is eligible, its propensity is computed using the concentration of the biomolecule
            n.observeConcentration(molecule).map { maybeValue ->
                maybeValue.fold(
                    ifEmpty = { 0.0 },
                    ifSome = {
                        if (it >= concentration) {
                            binomialCoefficientDouble(it.toInt(), FastMath.ceil(concentration).toInt())
                        } else {
                            0.0
                        }
                    },
                )
            }
        } ?: observe(0.0)

    override fun cloneCondition(
        newNode: Node<Double>,
        newReaction: Reaction<Double>,
    ): AbstractNeighborCondition<Double> = BiomolPresentInNeighbor(environment, node, molecule, concentration)

    override fun toString(): String = "$molecule >= $concentration in neighbor"

    private fun setUpObservability() {
        addObservableDependency(node.observeConcentration(molecule))
        setValidity(
            observeValidNeighbors().map { validNeighbors ->
                val current = environment.getNeighborhood(node).current
                validNeighbors
                    ?.takeIf { it.isNotEmpty() }?.entries
                    ?.filter { it.key.asPropertyOrNull<Double, CellProperty<*>>() != null }
                    ?.all { it.key in current && it.key.getConcentration(molecule) >= concentration }
                    ?: false
            },
        )
    }

    /**
     * Companion object for the [BiomolPresentInNeighbor] class.
     *
     * This object stores the `serialVersionUID` constant required for serialization purposes.
     */
    companion object {
        @Serial const val serialVersionUID: Long = 499903479123400111L
    }
}
