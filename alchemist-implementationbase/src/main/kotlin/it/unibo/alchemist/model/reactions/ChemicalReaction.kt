/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution

/**
 * A reaction whose rate is the product of its time distribution rate and all condition propensity contributions.
 *
 * @param T concentration type
 */
open class ChemicalReaction<T>(node: Node<T>, timeDistribution: TimeDistribution<T>) :
    AbstractReaction<T>(node, timeDistribution) {

    private var currentRate = 0.0

    final override val rate: Double get() = currentRate

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): ChemicalReaction<T> = makeClone {
        ChemicalReaction(node, timeDistribution.cloneOnNewNode(node, currentTime))
    }

    override fun onInitializationComplete(atTime: Time, environment: Environment<T, *>) {
        update(atTime)
    }

    /**
     * Subclasses overriding this method must invoke the base implementation to refresh [rate].
     */
    override fun updateInternalStatus(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) {
        currentRate = timeDistribution.rate
        for (condition in conditions) {
            val contribution = condition.getPropensityContribution().current
            require(contribution >= 0) { "Condition $condition returned a negative propensity contribution" }
            if (contribution == 0.0) {
                currentRate = 0.0
                break
            }
            currentRate *= contribution
        }
    }
}
