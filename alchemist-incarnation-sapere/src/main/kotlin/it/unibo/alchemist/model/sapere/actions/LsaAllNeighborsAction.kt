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
import org.apache.commons.math3.random.RandomGenerator

/**
 * Adds the LSA [molecule] to all the valid neighbors.
 * The molecule can be different from a neighbor to the other, because some special variables (e.g., #D) are
 * specific for each node: N molecules are instanced if there are N neighbors.
 * The [randomGenerator] is unused, and can be null.
 */
class LsaAllNeighborsAction(
    randomGenerator: RandomGenerator?,
    environment: Environment<List<ILsaMolecule>, *>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    molecule: ILsaMolecule,
) : LsaRandomNeighborAction(randomGenerator, environment, reaction, molecule) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): LsaAllNeighborsAction =
        LsaAllNeighborsAction(randomGenerator, environment, newReaction, molecule)

    override fun execute() {
        checkNotNull(nodes).forEach { target ->
            setSynthectics(target)
            setConcentration(target)
        }
    }

    override fun toString(): String = "*$molecule"
}
