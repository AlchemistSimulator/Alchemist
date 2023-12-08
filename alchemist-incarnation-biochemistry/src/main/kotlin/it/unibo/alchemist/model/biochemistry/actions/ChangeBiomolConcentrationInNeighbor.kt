/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.Iterables.randomElement
import org.apache.commons.math3.random.RandomGenerator

/**
 * Changes the concentration of a [Biomolecule] ([molecule]) in a randomly-selected compatible neighbor cell by
 * [deltaConcentration].
 */
class ChangeBiomolConcentrationInNeighbor(
    randomGenerator: RandomGenerator,
    environment: Environment<Double, *>,
    node: Node<Double>,
    val molecule: Biomolecule,
    val deltaConcentration: Double,
) : AbstractNeighborAction<Double>(node, environment, randomGenerator) {

    override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>) =
        ChangeBiomolConcentrationInNeighbor(randomGenerator, environment, node, molecule, deltaConcentration)

    override fun execute() {
        val neighborhood = environment.getNeighborhood(node)
        val validNeighbors = neighborhood.filter {
            it.asPropertyOrNull<Double, CellProperty<Euclidean2DPosition>>() != null &&
                (deltaConcentration > 0 || it.getConcentration(molecule) >= deltaConcentration)
        }
        execute(validNeighbors.randomElement(randomGenerator))
    }

    override fun execute(targetNode: Node<Double>) {
        targetNode.setConcentration(molecule, targetNode.getConcentration(molecule) + deltaConcentration)
    }

    override fun toString(): String {
        return if (deltaConcentration >= 0) {
            "add $deltaConcentration of $molecule in neighbor "
        } else {
            "remove " + -deltaConcentration + " of " + molecule + " in neighbor "
        }
    }
}
