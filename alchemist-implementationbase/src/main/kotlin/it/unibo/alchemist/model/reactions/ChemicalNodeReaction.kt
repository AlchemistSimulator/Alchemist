/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution

/**
 * Base for memoryless chemical reactions.
 *
 * The generic implementation accepts no conditions: concrete chemical families must define their accepted semantic
 * condition types and compute their own rate from typed model state. Construction rejects non-memoryless
 * distributions.
 *
 * @param T concentration type
 */
open class ChemicalNodeReaction<T>(node: Node<T>, timeDistribution: TimeDistribution<T>) :
    AbstractMarkovianNodeReaction<T>(node, timeDistribution) {

    private var currentRate = 0.0

    final override val rate: Double get() = currentRate

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): ChemicalNodeReaction<T> =
        makeClone(node, currentTime) { freshGenerator -> ChemicalNodeReaction(node, freshGenerator) }

    override fun onInitializationComplete(atTime: Time, environment: Environment<T, *>) {
        if (!isNewlyInstantiatedProgram && canExecute().current) {
            refreshReactionState(atTime, environment)
            scheduleNextOccurrenceAfterFiring(atTime)
        }
    }

    override fun refreshReactionState(currentTime: Time, environment: Environment<T, *>) {
        currentRate = computeRate(currentTime, environment)
        require(!currentRate.isNaN() && currentRate >= 0.0) { "Reaction $this computed an invalid rate: $currentRate" }
    }

    /** Computes the current chemical rate from reaction-specific typed state. */
    protected open fun computeRate(currentTime: Time, environment: Environment<T, *>): Double = baseRate

    /** The rate configured by the memoryless time distribution before chemical state is applied. */
    protected val baseRate: Double get() = super.rate

    override fun validateConditions(conditions: List<Condition<T>>) {
        require(conditions.isEmpty()) {
            "${javaClass.simpleName} must define an explicit accepted-condition contract before using $conditions"
        }
    }
}
