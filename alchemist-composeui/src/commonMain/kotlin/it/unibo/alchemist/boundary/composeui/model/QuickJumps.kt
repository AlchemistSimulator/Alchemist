/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

/**
 * How far the quick-jump buttons move the simulation.
 *
 * @property time the time jumps, by default 10, 100 and 1000 time units.
 * @property steps the step jumps, or null for one, two and ten steps per node.
 */
internal data class QuickJumps(val time: List<Double> = DEFAULT_TIME, val steps: List<Long>? = null) {
    init {
        require(time.all { it > 0 && it.isFinite() }) { "Time quick jumps must be positive, but were $time." }
        require(steps.orEmpty().all { it > 0 }) { "Step quick jumps must be positive, but were $steps." }
    }

    /**
     * The step jumps for [nodeCount] nodes.
     * Unless configured, they are one, two and ten steps per node, so that every node can act about that often,
     * without jumps of one step, the default of the "to step" box, or less.
     */
    fun stepDeltas(nodeCount: Int): List<Long> =
        steps ?: listOf(nodeCount.toLong(), 2L * nodeCount, TEN * nodeCount).filter { it > 1 }

    private companion object {
        const val TEN = 10L
        val DEFAULT_TIME: List<Double> = listOf(10.0, 100.0, 1000.0)
    }
}
