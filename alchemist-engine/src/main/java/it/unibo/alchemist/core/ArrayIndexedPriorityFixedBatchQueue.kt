/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import it.unibo.alchemist.model.Actionable

/**
 * Batched extension for ArrayIndexedPriorityQueue.
 * This implementation presents fixed size batches.
 *
 * @param <T> concentration type
 */
class ArrayIndexedPriorityFixedBatchQueue<T>(
    private val batchSize: Int,
    private val delegate: ArrayIndexedPriorityQueue<T> = ArrayIndexedPriorityQueue(),
) : Scheduler<T> by delegate, BatchedScheduler<T> {

    override fun getNextBatch(): List<Actionable<T>> {
        return if (delegate.tree.isNotEmpty()) {
            delegate.tree.subList(0, delegate.tree.size.coerceAtMost(batchSize))
        } else {
            emptyList()
        }
    }

    override fun updateReaction(reaction: Actionable<T>) {
        synchronized(this) {
            delegate.updateReaction(reaction)
        }
    }
}
