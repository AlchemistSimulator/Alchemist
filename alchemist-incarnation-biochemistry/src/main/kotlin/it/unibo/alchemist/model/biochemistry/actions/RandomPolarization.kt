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
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.math.hypot
import org.apache.commons.math3.random.RandomGenerator

/**
 * Polarizes a cell with a [CellProperty] in a random direction.
 */
class RandomPolarization(
    private val environment: Environment<Double, Euclidean2DPosition>,
    reaction: NodeReaction<Double>,
    randomGenerator: RandomGenerator,
) : AbstractRandomizableAction<Double>(reaction, randomGenerator) {
    private val cell: CellProperty<Euclidean2DPosition> =
        requireNotNull(targetNode.asPropertyOrNull<Double, CellProperty<Euclidean2DPosition>>()) {
            "Polarization can happen only in nodes with ${CellProperty::class.simpleName}"
        }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): RandomPolarization =
        RandomPolarization(environment, newReaction, randomGenerator)

    override fun execute() {
        val x = randomGenerator.nextFloat() - 0.5
        val y = randomGenerator.nextFloat() - 0.5
        val module = hypot(x, y)
        cell.addPolarizationVersor(
            when {
                x == 0.0 -> Euclidean2DPosition(0.0, 1.0)
                y == 0.0 -> Euclidean2DPosition(1.0, 0.0)
                else -> Euclidean2DPosition(x / module, y / module)
            },
        )
    }
}
