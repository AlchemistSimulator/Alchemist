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
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.EnvironmentNode
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import kotlin.math.abs
import org.apache.commons.math3.random.RandomGenerator

/**
 * Changes the concentration of [biomolecule] in the environment nodes around the node by [deltaConcentration].
 * The node must be an [EnvironmentNode] or have a [CellProperty].
 */
class ChangeBiomolConcentrationInEnv(
    private val environment: Environment<Double, *>,
    reaction: NodeReaction<Double>,
    private val biomolecule: Biomolecule,
    private val deltaConcentration: Double,
    randomGenerator: RandomGenerator,
) : AbstractRandomizableAction<Double>(reaction, randomGenerator) {
    /**
     * Removes one unit of [biomolecule] from the environment nodes around the node.
     */
    constructor(
        reaction: NodeReaction<Double>,
        biomolecule: Biomolecule,
        environment: Environment<Double, *>,
        randomGenerator: RandomGenerator,
    ) : this(environment, reaction, biomolecule, -1.0, randomGenerator)

    init {
        if (targetNode !is EnvironmentNode && targetNode.asPropertyOrNull<Double, CellProperty<*>>() == null) {
            throw UnsupportedOperationException(
                "This condition can be set only in Node with nodes with ${CellProperty::class.simpleName} or " +
                    EnvironmentNode::class.simpleName,
            )
        }
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): ChangeBiomolConcentrationInEnv =
        ChangeBiomolConcentrationInEnv(environment, newReaction, biomolecule, deltaConcentration, randomGenerator)

    override fun execute() {
        val surrounding = environment.getNeighborhood(targetNode).current.neighbors
            .filterIsInstance<EnvironmentNode>()
            .toMutableList()
        when {
            targetNode is EnvironmentNode -> changeConcentrationInRandomNodes(surrounding)
            surrounding.map { environment.getDistanceBetweenNodes(targetNode, it) }.distinct().size == 1 ->
                if (surrounding.map { it.getConcentration(biomolecule) }.distinct().size == 1) {
                    changeConcentrationInRandomNodes(surrounding)
                } else {
                    changeConcentrationInSortedNodes(surrounding.sortedBy { it.getConcentration(biomolecule) })
                }
            else -> changeConcentrationInSortedNodes(
                surrounding.sortedBy { environment.getDistanceBetweenNodes(targetNode, it) },
            )
        }
    }

    private fun changeConcentrationInSortedNodes(sorted: List<EnvironmentNode>) {
        if (deltaConcentration < 0) {
            var remaining = deltaConcentration
            for (target in sorted) {
                val concentration = target.getConcentration(biomolecule)
                if (concentration >= abs(remaining)) {
                    target.setConcentration(biomolecule, concentration + remaining)
                    break
                }
                // Remove all the molecules of the species from this node, then continue with the next one.
                remaining += concentration
                target.removeConcentration(biomolecule)
            }
        } else {
            val target = sorted.first()
            target.setConcentration(biomolecule, target.getConcentration(biomolecule) + deltaConcentration)
        }
    }

    private fun changeConcentrationInRandomNodes(candidates: MutableList<EnvironmentNode>) {
        if (deltaConcentration < 0) {
            var remaining = deltaConcentration
            while (remaining < 0) {
                val index = randomGenerator.nextInt(candidates.size)
                val target = candidates[index]
                val concentration = target.getConcentration(biomolecule)
                if (concentration >= abs(remaining)) {
                    target.setConcentration(biomolecule, concentration + remaining)
                    break
                }
                // Remove all the molecules of the species from this node, then continue with another one.
                remaining += concentration
                target.removeConcentration(biomolecule)
                candidates.removeAt(index)
            }
        } else {
            val target = candidates[randomGenerator.nextInt(candidates.size)]
            target.setConcentration(biomolecule, target.getConcentration(biomolecule) + deltaConcentration)
        }
    }

    override fun toString(): String = "add $deltaConcentration $biomolecule in env "
}
