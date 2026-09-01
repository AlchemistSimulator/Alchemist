/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.ReactionHost
import it.unibo.alchemist.model.Time

/**
 * A single-use reaction checked at one absolute [occurrence].
 *
 * Conditions do not alter scheduling and are observed only at [occurrence]. If they are all valid, they are
 * prepared and the actions execute. The event then unregisters itself from [host] whether the conditions were valid
 * or not. An action failure remains fail-fast and prevents successful completion of the event procedure.
 *
 * @param T concentration type
 * @param host model entity hosting the event
 * @param occurrence absolute occurrence time
 */
class AbsoluteEvent<T>(private val host: ReactionHost<T>, val occurrence: Time) : AbstractReaction<T>(occurrence) {

    init {
        require(occurrence.isFinite && occurrence >= Time.ZERO) {
            "An absolute event requires a finite, non-negative occurrence, got $occurrence"
        }
    }

    override val conditionsGateScheduling: Boolean = false

    override fun execute() {
        if (conditions.all { it.isValid().current }) {
            super.execute()
        }
        host.removeReaction(this)
    }

    override fun updateSchedulingAfterInvalidation(currentTime: Time) = Unit
}
