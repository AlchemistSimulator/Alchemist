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
import it.unibo.alchemist.model.ReactionHost
import it.unibo.alchemist.model.Time

/**
 * A single-use reaction scheduled at one absolute [occurrence].
 *
 * Events currently reject conditions because the policy for an invalid condition at the absolute occurrence is not
 * part of the model contract yet. Once its actions complete, the event unregisters itself from [host].
 *
 * @param T concentration type
 * @param host model entity hosting the event
 * @param occurrence absolute occurrence time
 */
class Event<T>(private val host: ReactionHost<T>, val occurrence: Time) : AbstractReaction<T>(occurrence) {

    init {
        require(occurrence.isFinite && occurrence >= Time.ZERO) {
            "An event requires a finite, non-negative absolute occurrence, got $occurrence"
        }
    }

    override fun execute() {
        super.execute()
        host.removeReaction(this)
    }

    override fun validateConditions(conditions: List<Condition<T>>) {
        require(conditions.isEmpty()) {
            "Events do not support conditions until invalid-at-occurrence semantics are defined: $conditions"
        }
    }

    override fun updateSchedulingAfterInvalidation(currentTime: Time) = Unit
}
