/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.util.DoubleInt

/**
 * A link between two nodes, smaller id first, packed in one number so that duplicates are cheap to drop.
 */
@JvmInline
internal value class EdgeKey private constructor(private val ids: DoubleInt) {
    /** The link as the viewport draws it. */
    val edge: ViewportEdge get() = ViewportEdge(fromNodeId = ids.high, toNodeId = ids.low)

    /**
     * A stable, well-spread score (the MurmurHash3 64-bit finalizer):
     * keeping the lowest scores gives a sample of links that does not change between frames.
     */
    val sampleScore: Long get() = ((ids.packed.mix() * FMIX_FIRST_MULTIPLIER).mix() * FMIX_SECOND_MULTIPLIER).mix()

    companion object {
        private const val FMIX_SHIFT = 33
        private const val FMIX_FIRST_MULTIPLIER = -0xae502812aa7333L
        private const val FMIX_SECOND_MULTIPLIER = -0x3b314601e57a13adL

        /** The link between two nodes, in either order; `null` if they are the same node. */
        fun of(firstNodeId: Int, secondNodeId: Int): EdgeKey? = when {
            firstNodeId == secondNodeId -> null
            firstNodeId < secondNodeId -> EdgeKey(DoubleInt(firstNodeId, secondNodeId))
            else -> EdgeKey(DoubleInt(secondNodeId, firstNodeId))
        }

        private fun Long.mix(): Long = this xor (this ushr FMIX_SHIFT)
    }
}
