/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.batch

import it.unibo.alchemist.model.Actionable
import java.util.Collections
import java.util.IdentityHashMap

/**
 * A manager for batching [Actionable] reschedule requests.
 * This is used to avoid redundant updates to the scheduler when multiple
 * dependencies change in a single simulation step.
 */
class BatchManager<T> {
    private val dirtyReactions = Collections.newSetFromMap(IdentityHashMap<Actionable<T>, Boolean>())
    private var isBatching = false

    /**
     * Executes the given [block] in a batch context.
     * Reschedule requests during this block will be collected and processed
     * only at the end of the block.
     *
     * @param onReschedule the callback to invoke for each unique reaction that requested a reschedule
     * @param block the block of code to execute
     */
    fun useBatch(onReschedule: (Actionable<T>) -> Unit, block: () -> Unit) {
        val wasBatching = isBatching
        isBatching = true
        return try {
            block()
        } finally {
            isBatching = wasBatching
            if (!isBatching) {
                val toProcess = dirtyReactions.toList()
                dirtyReactions.clear()
                toProcess.forEach(onReschedule)
            }
        }
    }

    /**
     * Records a reschedule request for the given [reaction].
     * If a batch is active, the request is collected and processed later.
     * Otherwise, the [otherwise] block is executed immediately.
     *
     * @param reaction the reaction that requested a reschedule
     * @param otherwise the block to execute if no batch is active
     */
    fun requestReschedule(reaction: Actionable<T>, otherwise: () -> Unit) {
        if (isBatching) {
            dirtyReactions.add(reaction)
        } else {
            otherwise()
        }
    }
}
