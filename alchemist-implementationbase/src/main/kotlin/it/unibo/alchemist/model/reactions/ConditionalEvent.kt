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
 * A single-use event scheduled by condition validity and a [timeDistribution].
 *
 * No sample is drawn while the conditions are invalid. Becoming valid draws one delay from [timeDistribution]. If
 * the conditions become invalid before execution, that putative occurrence is discarded and the event publishes
 * [Time.INFINITY]. The next invalid-to-valid transition draws a new delay. Repeated updates while validity remains
 * true do not redraw the occurrence. After successful action execution, the event unregisters itself from [node].
 *
 * @param T concentration type
 * @param node node hosting the event
 * @param timeDistribution generator for delays from each enabling transition
 */
class ConditionalEvent<T>(node: Node<T>, timeDistribution: TimeDistribution<T>) :
    GenericReaction<T>(node, timeDistribution) {

    private var armed = false

    override fun afterInitializationComplete(atTime: Time, environment: Environment<T, *>) {
        super.afterInitializationComplete(atTime, environment)
        armIfValid(atTime)
    }

    override fun initializeNewProgramScheduling(currentTime: Time) {
        armIfValid(currentTime)
    }

    override fun updateSchedulingAfterInvalidation(currentTime: Time) {
        armIfValid(currentTime)
    }

    override fun suspendScheduling() {
        armed = false
        super.suspendScheduling()
    }

    override fun executeReaction() {
        super.executeReaction()
        node.removeReaction(this)
    }

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): ConditionalEvent<T> =
        makeClone(node, currentTime) { freshGenerator -> ConditionalEvent(node, freshGenerator) }

    private fun armIfValid(currentTime: Time) {
        if (!armed && canExecute().current) {
            super.updateSchedulingAfterInvalidation(currentTime)
            armed = true
        }
    }
}
