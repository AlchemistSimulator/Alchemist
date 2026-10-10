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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode

/**
 * A SAPERE agent aware of the [environment] hosting its node.
 *
 * @param P [Position] type
 */
abstract class AbstractSAPEREEnvironmentAgent<P : Position<P>> protected constructor(
    /**
     * The current environment.
     */
    protected val environment: Environment<List<ILsaMolecule>, P>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    modifiedMolecules: List<ILsaMolecule>,
) : AbstractSAPEREAgent(reaction, modifiedMolecules) {
    /**
     * The current position of the node.
     */
    protected val currentPosition: P get() = environment.getCurrentPosition(targetNode)

    /**
     * The current neighborhood of the node.
     */
    protected val localNeighborhood: Neighborhood<List<ILsaMolecule>>
        get() = environment.getNeighborhood(targetNode).current

    /**
     * Returns the position of [node].
     */
    protected fun getPosition(node: Node<List<ILsaMolecule>>): P = environment.getCurrentPosition(node)

    /**
     * Returns the current neighborhood of [node].
     */
    protected fun getNeighborhood(node: ILsaNode): Neighborhood<List<ILsaMolecule>> =
        environment.getNeighborhood(node).current
}
