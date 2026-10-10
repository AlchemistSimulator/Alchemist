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

/**
 * A SAPERE agent moving the node hosting it in the environment.
 * If a [molecule] template is provided, the agent modifies (locally!) the molecules matching it,
 * otherwise it does not modify any molecule (e.g., an agent that just moves a node).
 *
 * @param P [Position] type
 */
abstract class AbstractSAPEREMoveNodeAgent<P : Position<P>> @JvmOverloads constructor(
    environment: Environment<List<ILsaMolecule>, P>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    molecule: ILsaMolecule? = null,
) : AbstractSAPEREEnvironmentAgent<P>(environment, reaction, listOfNotNull(molecule)) {
    /**
     * Moves the node by [direction].
     */
    protected fun move(direction: P) =
        environment.moveNodeTo(targetNode, environment.getCurrentPosition(targetNode).plus(direction.coordinates))
}
