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
import kotlin.math.abs

/**
 * Batched extension for ArrayIndexedPriorityQueue.
 * This implementation uses epsilon-sensitivity
 * in order to build the next batch to process. Events will be added to the batch
 * while | tau(e1) - tau(e2) | < epsilon.
 *
 * @param <T> concentration type
 */
class ArrayIndexedPriorityEpsilonBatchQueue<T>(
    private val epsilon: Double,
    private val delegate: ArrayIndexedPriorityQueue<T> = ArrayIndexedPriorityQueue(),
) : Scheduler<T> by delegate, BatchedScheduler<T> {

    override fun getNextBatch(): List<Actionable<T>> {
        if (delegate.tree.isEmpty()) {
            return emptyList()
        }
        if (delegate.tree.size == 1) {
            val result = mutableListOf<Actionable<T>>()
            result.add(delegate.tree[0])
            return result
        }

        val result = mutableListOf<Actionable<T>>()
        val prev = delegate.tree[0]
        result.add(prev)
        for (next in delegate.tree.subList(1, delegate.tree.size)) {
            if (abs(next.tau.toDouble() - prev.tau.toDouble()) >= epsilon) {
                break
            } else {
                result.add(next)
            }
        }
        return result
    }

    override fun updateReaction(reaction: Actionable<T>) {
        synchronized(this) {
            delegate.updateReaction(reaction)
        }
    }
}
