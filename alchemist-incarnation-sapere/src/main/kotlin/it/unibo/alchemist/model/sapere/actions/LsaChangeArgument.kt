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
import it.unibo.alchemist.model.sapere.dsl.impl.ConstTreeNode
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.lang.HashString

/**
 * Picks at random a type in [listTarget] other than the one matched by the variable `OldType`,
 * and adds it to the matches as the variable [targetVariable].
 * The execution has no effect on the influenced molecules of the reaction.
 */
class LsaChangeArgument(
    reaction: NodeReaction<List<ILsaMolecule>>,
    listTarget: Array<String>,
    targetVariable: String,
    private val randomGenerator: RandomGenerator,
) : AbstractSAPEREAgent(reaction) {
    private val newTargetVariable = HashString(targetVariable)
    private val targets: List<String> = listTarget.toList()

    override fun execute() {
        val candidates = targets.toMutableList()
        val oldType = checkNotNull(matches)[OLD].toString()
        check(candidates.remove(oldType)) { "Cannot remove $oldType from $candidates" }
        if (candidates.isNotEmpty()) {
            val newTarget = candidates[(candidates.size * randomGenerator.nextDouble()).toInt()]
            addMatch(newTargetVariable, ConstTreeNode(HashString(newTarget)))
        }
    }

    override fun toString(): String = "Find $newTargetVariable"

    private companion object {
        private val OLD = HashString("OldType")
    }
}
