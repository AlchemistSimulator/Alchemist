/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.actions.LsaAscendingAgent.Companion.LIMIT
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Moves the node towards the neighbor with the lowest value of the [template] LSA (typically a gradient)
 * in position [pos], by at most [LIMIT] along each axis, unless that neighbor is active.
 *
 * @param P [it.unibo.alchemist.model.Position] type
 */
class LsaAscendingAgent<P : Position2D<P>>(
    reaction: NodeReaction<List<ILsaMolecule>>,
    environment: Environment<List<ILsaMolecule>, P>,
    private val template: LsaMolecule,
    private val pos: Int,
) : AbstractSAPEREMoveNodeAgent<P>(environment, reaction) {
    override fun execute() {
        var minGrad = Double.MAX_VALUE
        var targetPosition: P? = null
        var bestNode: Node<List<ILsaMolecule>>? = null
        for (neighbor in localNeighborhood.neighbors) {
            val lsaNeighbor = neighbor as ILsaNode
            for (grad in lsaNeighbor.getConcentration(template)) {
                val valueGrad = getLSAArgumentAsDouble(grad, pos)
                if (valueGrad <= minGrad) {
                    minGrad = valueGrad
                    targetPosition = getPosition(lsaNeighbor)
                    bestNode = lsaNeighbor
                }
            }
        }
        if (bestNode == null || bestNode.contains(ACTIVE) || targetPosition == null) {
            return
        }
        val myPosition = currentPosition
        val dx = (targetPosition.x - myPosition.x).let { if (it > 0) min(LIMIT, it) else max(-LIMIT, it) }
        val dy = (targetPosition.y - myPosition.y).let { if (it > 0) min(LIMIT, it) else max(-LIMIT, it) }
        val moveH = dx.absoluteValue > 0
        val moveV = dy.absoluteValue > 0
        if (moveH || moveV) {
            move(environment.makePosition(if (moveH) dx else 0.0, if (moveV) dy else 0.0))
        }
    }

    private companion object {
        /*
         * An agent can move at most of LIMIT along each axis.
         */
        private const val LIMIT = 0.1
        private val ACTIVE: ILsaMolecule = LsaMolecule("active")
    }
}
