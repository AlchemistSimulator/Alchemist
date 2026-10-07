/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.biochemistry.CircularCellProperty
import it.unibo.alchemist.model.biochemistry.CircularDeformableCellProperty
import it.unibo.alchemist.model.biochemistry.EnvironmentSupportingDeformableCells
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.math.hypot
import org.danilopianini.lang.MathUtils

/**
 * Models the tension polarization of a cell with a [CircularDeformableCellProperty]
 * in an [EnvironmentSupportingDeformableCells]: the cell is pushed away from the overlapping cells.
 */
class CellTensionPolarization(
    private val environment: EnvironmentSupportingDeformableCells<Euclidean2DPosition>,
    reaction: NodeReaction<Double>,
) : AbstractLocalAction<Double>(reaction) {
    private val deformableCell: CircularDeformableCellProperty = requireNotNull(targetNode.deformableCell) {
        "The node must have a ${CircularDeformableCellProperty::class.simpleName}"
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): CellTensionPolarization =
        CellTensionPolarization(environment, newReaction)

    override fun execute() {
        val position = environment.getCurrentPosition(targetNode)
        val pushForces = environment
            .getNodesWithinRange(targetNode, environment.maxDiameterAmongCircularDeformableCells)
            .filter { overlaps(it) }
            .map { pushForce(it, position) }
        val resultX = pushForces.sumOf { it.x }
        val resultY = pushForces.sumOf { it.y }
        val polarizationVersor = when (val module = hypot(resultX, resultY)) {
            0.0 -> environment.makePosition(0, 0)
            else -> environment.makePosition(resultX / module, resultY / module)
        }
        deformableCell.addPolarizationVersor(polarizationVersor)
    }

    /**
     * Only cells with a circular area can overlap: deformable cells may overlap up to their maximum radius,
     * simple cells up to their radius.
     */
    private fun overlaps(other: Node<Double>): Boolean {
        val circularCell = other.circularCell ?: return false
        val maxDistance = deformableCell.maximumRadius + (other.deformableCell?.maximumRadius ?: circularCell.radius)
        return environment.getDistanceBetweenNodes(targetNode, other) < maxDistance
    }

    /**
     * The versor pushing this cell away from [other], scaled by the tension intensity, between 0 and 1.
     */
    private fun pushForce(other: Node<Double>, position: Euclidean2DPosition): Euclidean2DPosition {
        val otherDeformable = other.deformableCell
        val otherMaxRadius = otherDeformable?.maximumRadius ?: checkNotNull(other.circularCell).radius
        val otherMinRadius = otherDeformable?.radius ?: otherMaxRadius
        val maxRadius = deformableCell.maximumRadius
        val minRadius = deformableCell.radius
        val intensity = if (
            MathUtils.fuzzyEquals(otherMaxRadius, otherMinRadius) && MathUtils.fuzzyEquals(maxRadius, minRadius)
        ) {
            // Rigid cells push at full intensity.
            1.0
        } else {
            val maxRadiusSum = otherMaxRadius + maxRadius
            (maxRadiusSum - environment.getDistanceBetweenNodes(other, targetNode)) /
                (maxRadiusSum - otherMinRadius - minRadius)
        }
        val otherPosition = environment.getCurrentPosition(other)
        val deltaX = position.x - otherPosition.x
        val deltaY = position.y - otherPosition.y
        val module = hypot(deltaX, deltaY)
        return if (intensity == 0.0 || module == 0.0) {
            environment.makePosition(0, 0)
        } else {
            environment.makePosition(intensity * deltaX / module, intensity * deltaY / module)
        }
    }

    private companion object {
        private val Node<Double>.deformableCell: CircularDeformableCellProperty?
            get() = asPropertyOrNull<Double, CircularDeformableCellProperty>()

        private val Node<Double>.circularCell: CircularCellProperty?
            get() = asPropertyOrNull<Double, CircularCellProperty>()
    }
}
