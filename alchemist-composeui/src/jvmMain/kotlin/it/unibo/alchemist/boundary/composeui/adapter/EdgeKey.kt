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
import org.danilopianini.noncryptohash.xxhash.XxHash32

/**
 * A link between two nodes, smaller id first, packed in one number.
 * Its hash is well spread, so that sets of links stay fast even when nearby nodes are linked.
 */
internal class EdgeKey private constructor(private val ids: DoubleInt) {
    /**
     * A stable, well-spread score (the XXH32 hash of the ids):
     * keeping the lowest scores gives a sample of links that does not change between frames.
     */
    val sampleScore: Int = HASH.newDigest().update(ids.packed).digestInt()

    /** The link as the viewport draws it. */
    val edge: ViewportEdge get() = ViewportEdge(fromNodeId = ids.high, toNodeId = ids.low)

    override fun equals(other: Any?): Boolean = other is EdgeKey && other.ids == ids

    override fun hashCode(): Int = sampleScore

    companion object {
        private val HASH = XxHash32()

        /** The link between two nodes, in either order; `null` if they are the same node. */
        fun of(firstNodeId: Int, secondNodeId: Int): EdgeKey? = when {
            firstNodeId == secondNodeId -> null
            firstNodeId < secondNodeId -> EdgeKey(DoubleInt(firstNodeId, secondNodeId))
            else -> EdgeKey(DoubleInt(secondNodeId, firstNodeId))
        }
    }
}
