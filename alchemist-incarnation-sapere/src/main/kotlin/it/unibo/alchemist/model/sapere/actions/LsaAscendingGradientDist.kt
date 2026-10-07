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
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.dsl.impl.AST
import it.unibo.alchemist.model.sapere.dsl.impl.Expression
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode

/**
 * Moves the response LSA to the last neighbor found with a lower gradient distance,
 * adding the distance to that neighbor to the distance carried by the response.
 *
 * @param P position type
 */
class LsaAscendingGradientDist<P : Position<P>>(
    environment: Environment<List<ILsaMolecule>, P>,
    reaction: NodeReaction<List<ILsaMolecule>>,
) : AbstractSAPERENeighborAgent<P>(environment, reaction, MOLRESPONSE) {
    private lateinit var neighborhood: Neighborhood<List<ILsaMolecule>>

    init {
        environment.getNeighborhood(targetNode).onChange(this) { neighborhood = it }
    }

    override fun execute() {
        var minGrad = getLSAArgumentAsDouble(lsaNode.getConcentration(MOLGRAD)[0], POS)
        val targets = mutableListOf<LsaNode>()
        for (neighbor in neighborhood.neighbors) {
            val lsaNeighbor = neighbor as LsaNode
            for (grad in lsaNeighbor.getConcentration(MOLGRAD)) {
                val valueGrad = grad.getArg(POS).calculate(null).getValue(null) as Double
                if (valueGrad <= minGrad) {
                    minGrad = valueGrad
                    targets.add(lsaNeighbor)
                }
            }
        }
        if (targets.isNotEmpty()) {
            val target = targets.last()
            val distance = currentPosition.distanceTo(getPosition(target))
            val response = MOLRESPONSE.allocateVar(matches)
            val oldDistance = response.removeAt(response.size - 1).rootNodeData as Double
            response.add(Expression(AST(NumTreeNode(distance + oldDistance))))
            target.setConcentration(LsaMolecule(response))
        }
    }

    private companion object {
        private val MOLGRAD: ILsaMolecule = LsaMolecule("grad, req, Type, Distance, Time")
        private val MOLRESPONSE: ILsaMolecule = LsaMolecule("response, Req, Ser, MD, D")
        private const val POS = 3
    }
}
