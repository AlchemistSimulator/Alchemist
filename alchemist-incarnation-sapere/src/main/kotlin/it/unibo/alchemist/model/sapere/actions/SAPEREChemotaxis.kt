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
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode

/**
 * A chemotaxis implementation for SAPERE, namely, an agent able to move the [response] molecule towards the node
 * whose ID is in position [idPosition] of the matched [gradient] template.
 *
 * @param P position type
 */
class SAPEREChemotaxis<P : Position<P>>(
    environment: Environment<List<ILsaMolecule>, P>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    private val response: ILsaMolecule,
    private val gradient: ILsaMolecule,
    private val idPosition: Int,
) : AbstractSAPERENeighborAgent<P>(environment, reaction, response) {
    init {
        require(idPosition >= 0) { "idPosition must be non-negative, but was: $idPosition" }
    }

    override fun execute() {
        val destination = environment.getNodeByID(getLSAArgumentAsInt(gradient, idPosition)) as ILsaNode
        allocateAndInject(response, destination)
    }
}
