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
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.actions.LsaAscendingAgent.Companion.LIMIT
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule

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
        val target = localNeighborhood.neighbors
            .flatMap { neighbor ->
                (neighbor as ILsaNode).getConcentration(template).map { neighbor to getLSAArgumentAsDouble(it, pos) }
            }
            // NaN and +∞ are never the lowest gradient; on ties, the last neighbor wins.
            .filter { (_, gradient) -> gradient <= Double.MAX_VALUE }
            .reduceOrNull { best, candidate -> if (candidate.second <= best.second) candidate else best }
            ?.first
            ?.takeUnless { it.contains(ACTIVE) }
            ?.let(::getPosition)
        if (target != null) {
            val dx = (target.x - currentPosition.x).coerceIn(-LIMIT, LIMIT)
            val dy = (target.y - currentPosition.y).coerceIn(-LIMIT, LIMIT)
            if (dx != 0.0 || dy != 0.0) {
                move(environment.makePosition(dx, dy))
            }
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
