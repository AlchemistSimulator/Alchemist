/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies.speed

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy

/**
 * Similar to [ConstantSpeed], but takes into consideration the mean delay of the time distribution
 * instead of the rate of the reaction: every movement covers the distance traveled at [maxSpeed]
 * in the expected time between two occurrences.
 * The mean delay must be finite and positive, both when this strategy is built and at every movement.
 */
class GloballyConstantSpeed<T, P : Position<P>>(reaction: NodeReaction<*>, private val maxSpeed: Double) :
    SpeedSelectionStrategy<T, P> {
    private val timeDistributedReaction = requireNotNull(reaction as? TimeDistributedReaction<*>) {
        "$reaction is not driven by a time distribution"
    }

    init {
        val meanDelay = timeDistributedReaction.timeDistribution.meanDelay
        verifyMeanDelayValidity(meanDelay)
    }

    override fun cloneIfNeeded(destination: Node<T>, reaction: NodeReaction<T>): GloballyConstantSpeed<T, P> =
        GloballyConstantSpeed(reaction, maxSpeed)

    override fun getNodeMovementLength(target: P?): Double {
        val meanDelay = timeDistributedReaction.timeDistribution.meanDelay
        verifyMeanDelayValidity(meanDelay)
        return maxSpeed * meanDelay
    }

    private fun verifyMeanDelayValidity(meanDelay: Double) =
        require(meanDelay.isFinite() && meanDelay > 0) {
            "${this::class.simpleName} requires a finite and positive mean delay, " +
                "but ${timeDistributedReaction.timeDistribution} has $meanDelay"
        }
}
