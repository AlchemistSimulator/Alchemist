/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.EnvironmentNode
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.math.hypot

/**
 * Models the chemotactic polarization of a cell: the polarization versor of the cell is directed towards the
 * highest concentration of [biomolecule] in the neighboring environment nodes if [ascendGrad] is `up`,
 * or in the opposite direction if it is `down`.
 */
class ChemotacticPolarization(
    private val environment: Environment<Double, Euclidean2DPosition>,
    reaction: NodeReaction<Double>,
    private val biomolecule: Biomolecule,
    private val ascendGrad: String,
) : AbstractLocalAction<Double>(reaction) {
    /**
     * Builds the action from the [biomolecule] name.
     */
    constructor(
        environment: Environment<Double, Euclidean2DPosition>,
        reaction: NodeReaction<Double>,
        biomolecule: String,
        ascendGrad: String,
    ) : this(environment, reaction, Biomolecule(biomolecule), ascendGrad)

    private val cell: CellProperty<Euclidean2DPosition> =
        requireNotNull(targetNode.asPropertyOrNull<Double, CellProperty<Euclidean2DPosition>>()) {
            "This action can't be added to nodes with no ${CellProperty::class.simpleName}"
        }

    private val ascend: Boolean = when {
        "up".equals(ascendGrad, ignoreCase = true) -> true
        "down".equals(ascendGrad, ignoreCase = true) -> false
        else -> throw IllegalArgumentException("Possible imput string are only up or down")
    }

    private var neighbors: List<Node<Double>> = emptyList()

    init {
        environment.getNeighborhood(targetNode).onChange(this) { neighborhood ->
            neighbors = neighborhood.neighbors.filter { it is EnvironmentNode && it.contains(biomolecule) }
        }
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): ChemotacticPolarization =
        ChemotacticPolarization(environment, newReaction, biomolecule, ascendGrad)

    override fun execute() {
        val candidates = neighbors
        val position = environment.getCurrentPosition(targetNode)
        val maxConcentrationNode = candidates.maxByOrNull { it.getConcentration(biomolecule) }
        when {
            maxConcentrationNode == null -> cell.addPolarizationVersor(Euclidean2DPosition.zero)
            environment.getCurrentPosition(maxConcentrationNode) == position ->
                cell.addPolarizationVersor(environment.makePosition(0, 0))
            else -> {
                val polarization = weightedAverageVectors(candidates, position)
                val module = hypot(polarization.x, polarization.y)
                cell.addPolarizationVersor(
                    when {
                        module == 0.0 -> polarization
                        ascend -> environment.makePosition(polarization.x / module, polarization.y / module)
                        else -> environment.makePosition(-polarization.x / module, -polarization.y / module)
                    },
                )
            }
        }
    }

    private fun weightedAverageVectors(
        candidates: List<Node<Double>>,
        position: Euclidean2DPosition,
    ): Euclidean2DPosition = candidates.fold(Euclidean2DPosition.zero) { result, neighbor ->
        val neighborPosition = environment.getCurrentPosition(neighbor)
        val deltaX = neighborPosition.x - position.x
        val deltaY = neighborPosition.y - position.y
        val module = hypot(deltaX, deltaY)
        val concentration = neighbor.getConcentration(biomolecule)
        Euclidean2DPosition(result.x + concentration * deltaX / module, result.y + concentration * deltaY / module)
    }
}
