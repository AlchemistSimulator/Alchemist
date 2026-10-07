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
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.lang.HashString

/**
 * Counts the neighbors containing in their LSA space a molecule matching [moleculeToCount], and adds the result to
 * the matches as the variable [countVariable].
 * The execution has no effect on the set of influenced molecules of the reaction.
 * The [randomGenerator] is unused.
 */
class LsaCountNeighborsAction(
    private val environment: Environment<List<ILsaMolecule>, *>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    private val moleculeToCount: ILsaMolecule,
    countVariable: HashString,
    private val randomGenerator: RandomGenerator,
) : AbstractSAPEREAgent(reaction) {
    private val countVariable = HashString(countVariable)

    /**
     * Builds the action with the name of the counting variable as String.
     */
    constructor(
        environment: Environment<List<ILsaMolecule>, *>,
        reaction: NodeReaction<List<ILsaMolecule>>,
        moleculeToCount: ILsaMolecule,
        countVariable: String,
        randomGenerator: RandomGenerator,
    ) : this(environment, reaction, moleculeToCount, HashString(countVariable), randomGenerator)

    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): LsaCountNeighborsAction =
        LsaCountNeighborsAction(environment, newReaction, moleculeToCount, countVariable, randomGenerator)

    override fun execute() {
        val template = LsaMolecule(moleculeToCount.allocateVar(matches))
        val count = environment.getNeighborhood(targetNode).current.neighbors
            .count { it.getConcentration(template).isNotEmpty() }
        checkNotNull(matches)[countVariable] = NumTreeNode(count.toDouble())
    }

    override fun toString(): String = "Count $countVariable"
}
