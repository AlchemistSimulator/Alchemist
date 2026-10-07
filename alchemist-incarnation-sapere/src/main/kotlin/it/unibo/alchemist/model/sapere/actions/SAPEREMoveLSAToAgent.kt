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
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode

/**
 * Matches [template], removes a single instance of it from the current node, and moves it to [destination].
 * Since the destination could be anywhere in the system, this action may trigger a large number of updates,
 * slowing down the whole simulation. Handle with care.
 */
class SAPEREMoveLSAToAgent(
    reaction: NodeReaction<List<ILsaMolecule>>,
    private val destination: ILsaNode,
    private val template: ILsaMolecule,
) : AbstractSAPEREAgent(reaction, template) {
    /**
     * Builds the action from the [destinationId] of the destination node in the [environment].
     * This is the constructor that should be called from the DSL.
     */
    constructor(
        environment: Environment<*, *>,
        reaction: NodeReaction<List<ILsaMolecule>>,
        destinationId: Int,
        template: ILsaMolecule,
    ) : this(reaction, environment.getNodeByID(destinationId) as ILsaNode, template)

    override fun execute() {
        val instance = allocateVarsAndBuildLSA(template)
        lsaNode.removeConcentration(instance)
        destination.setConcentration(instance)
    }
}
