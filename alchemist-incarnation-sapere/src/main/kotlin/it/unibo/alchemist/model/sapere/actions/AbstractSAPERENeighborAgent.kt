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
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule

/**
 * A SAPERE agent that modifies something on neighboring nodes.
 *
 * @param P position type
 */
abstract class AbstractSAPERENeighborAgent<P : Position<P>> private constructor(
    environment: Environment<List<ILsaMolecule>, P>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    modifiedMolecules: List<ILsaMolecule>,
) : AbstractSAPEREEnvironmentAgent<P>(environment, reaction, modifiedMolecules) {
    /**
     * Creates an agent that only modifies molecules matching the template [m1].
     */
    constructor(
        environment: Environment<List<ILsaMolecule>, P>,
        reaction: NodeReaction<List<ILsaMolecule>>,
        m1: ILsaMolecule,
    ) : this(environment, reaction, listOf(m1))

    /**
     * Creates an agent that only modifies molecules matching the templates [m1] and [m2].
     */
    constructor(
        environment: Environment<List<ILsaMolecule>, P>,
        reaction: NodeReaction<List<ILsaMolecule>>,
        m1: ILsaMolecule,
        m2: ILsaMolecule,
    ) : this(environment, reaction, listOf(m1, m2))

    /**
     * Creates an agent that only modifies molecules matching the templates [m1], [m2], and [m3].
     */
    constructor(
        environment: Environment<List<ILsaMolecule>, P>,
        reaction: NodeReaction<List<ILsaMolecule>>,
        m1: ILsaMolecule,
        m2: ILsaMolecule,
        m3: ILsaMolecule,
    ) : this(environment, reaction, listOf(m1, m2, m3))

    /**
     * Moves the node by [direction].
     */
    protected fun move(direction: P) {
        environment.moveNodeTo(
            targetNode,
            environment.getCurrentPosition(targetNode).plus(direction.coordinates),
        )
    }

    /**
     * Allocates the variables of [molecule] using the current matches, and injects the result in [destination].
     */
    protected open fun allocateAndInject(molecule: ILsaMolecule, destination: ILsaNode) {
        destination.setConcentration(LsaMolecule(molecule.allocateVar(matches)))
    }
}
