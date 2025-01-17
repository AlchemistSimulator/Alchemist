/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.reactions.AbstractReaction

class TestTriggerReaction<T, P : Position<P>>(
    environment: Environment<T, P>,
    timeDistribution: TimeDistribution<T>,
    node: Node<T>,
) : AbstractReaction<T>(node, timeDistribution) {
    private var executed = false

    override fun updateInternalStatus(
        currentTime: Time?,
        hasBeenExecuted: Boolean,
        environment: Environment<T?, *>?,
    ) {
        when (executed) {
            true -> throw IllegalStateException("Reaction already executed")
            false -> executed = true
        }
    }

    override fun cloneOnNewNode(
        node: Node<T?>,
        currentTime: Time,
    ): Reaction<T?> {
        TODO("Not yet implemented")
    }
}
