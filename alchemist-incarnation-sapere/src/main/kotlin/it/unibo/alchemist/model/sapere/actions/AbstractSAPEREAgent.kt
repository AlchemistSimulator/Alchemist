/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.sapere.ILsaMolecule

/**
 * A SAPERE agent stub, which can only modify the molecules matching the templates passed at construction time.
 * Agents are not meant to be cloned.
 */
abstract class AbstractSAPEREAgent protected constructor(
    reaction: NodeReaction<List<ILsaMolecule>>,
    modifiedMolecules: List<ILsaMolecule>,
) : AbstractLsaAction(reaction, modifiedMolecules) {
    /**
     * Creates an agent that does not modify any molecule (e.g., an agent that just moves a node).
     */
    constructor(reaction: NodeReaction<List<ILsaMolecule>>) : this(reaction, emptyList())

    /**
     * Creates an agent that only modifies molecules matching the template [m1].
     */
    constructor(reaction: NodeReaction<List<ILsaMolecule>>, m1: ILsaMolecule) : this(reaction, listOf(m1))

    /**
     * Creates an agent that only modifies molecules matching the templates [m1] and [m2].
     */
    constructor(
        reaction: NodeReaction<List<ILsaMolecule>>,
        m1: ILsaMolecule,
        m2: ILsaMolecule,
    ) : this(reaction, listOf(m1, m2))

    /**
     * Creates an agent that only modifies molecules matching the templates [m1], [m2], and [m3].
     */
    constructor(
        reaction: NodeReaction<List<ILsaMolecule>>,
        m1: ILsaMolecule,
        m2: ILsaMolecule,
        m3: ILsaMolecule,
    ) : this(reaction, listOf(m1, m2, m3))

    override fun toString(): String = javaClass.simpleName

    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): AbstractSAPEREAgent =
        throw UnsupportedOperationException(
            "SAPERE Agents are not meant to be cloned. If you want, implement cloneOnNewNode yourself.",
        )
}
