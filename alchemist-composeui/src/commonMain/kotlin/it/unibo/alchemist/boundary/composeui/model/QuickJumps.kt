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
 */
internal object QuickJumps {
    private const val THOUSAND = 1000.0
    private const val TEN = 10L

    /** Time jumps: 10, 100, and 1000 time units; a jump of one is the default of the "to time" box. */
    val timeDeltas: List<Double> = generateSequence(TEN.toDouble()) { it * TEN }.takeWhile { it <= THOUSAND }.toList()

    /**
     * Step jumps: one, two and ten steps per node, so that every node can act about that often.
     * Jumps of one step, the default of the "to step" box, or less are dropped.
     */
    fun stepDeltas(nodeCount: Int): List<Long> = listOf(nodeCount.toLong(), 2L * nodeCount, TEN * nodeCount)
        .filter { it > 1 }
}
