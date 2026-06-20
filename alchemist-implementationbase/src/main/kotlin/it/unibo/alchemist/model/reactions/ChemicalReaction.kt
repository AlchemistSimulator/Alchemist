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
import java.io.Serial
import java.util.function.Supplier
import javax.annotation.Nonnull

/**
 * @param <T> concentration type
 * @param node
 * node
 * @param timeDistribution
 * time distribution
</T> */
open class ChemicalReaction<T>(node: Node<T>, timeDistribution: TimeDistribution<T>) : AbstractReaction<T>(
    node,
    timeDistribution,
    rateEquation = { propensityFactors, time -> time + propensityFactors.fold(1.0) { a, b -> a * b } }
) {
    var rate: Double = 0.0
        private set

    /**
     * {@inheritDoc}
     */
    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): ChemicalReaction<T> {
        return makeClone<ChemicalReaction<T>> {
            ChemicalReaction<T>(
                node,
                this@ChemicalReaction.timeDistribution.cloneOnNewNode(node, currentTime)
            )
        }
    }

    override fun onInitializationComplete(@Nonnull atTime: Time, @Nonnull environment: Environment<T?, *>) {
        update(atTime, true, environment)
    }

    /**
     * Subclasses must call super.updateInternalStatus for the rate to get updated in case of method override.
     */
    override fun updateInternalStatus(
        currentTime: Time?,
        hasBeenExecuted: Boolean,
        environment: Environment<T?, *>?
    ) {
        this.rate = this@ChemicalReaction.timeDistribution.getRate()
        for (cond in conditions) {
            val v: Double = cond.getPropensityContribution()
            if (v == 0.0) {
                this.rate = 0.0
                break
            }
            check(!(v < 0)) { "Condition " + cond + " returned a negative propensity conditioning value" }
            this.rate *= cond.getPropensityContribution()
        }
    }

    companion object {
        @Serial
        private val serialVersionUID = -5260452049415003046L
    }
}
