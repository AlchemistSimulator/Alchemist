/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies.speed

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy

/**
 * Similar to [ConstantSpeed] but takes in consideration the time distribution's rate instead of the reaction's rate.
 */
class GloballyConstantSpeed<T, P : Position<P>>(
    private val reaction: Reaction<*>,
    private val maxSpeed: Double,
) : SpeedSelectionStrategy<T, P> {
    override fun getNodeMovementLength(target: P) = maxSpeed / reaction.timeDistribution.rate

    override fun cloneIfNeeded(destination: Node<T>, reaction: Reaction<T>): GloballyConstantSpeed<T, P> =
        GloballyConstantSpeed(reaction, maxSpeed)
}
