/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import org.danilopianini.noncryptohash.xxhash.XxHash32

/** A link between two nodes, in either direction. */
internal class LinkKey private constructor(private val lower: Int, private val higher: Int) {
    /**
     * A stable, well-spread score, the XXH32 hash of the ids:
     * keeping the lowest scores gives a sample of links that does not change between snapshots.
     */
    val sampleScore: Int = HASH.newDigest().update((lower.toLong() shl Int.SIZE_BITS) or higher.toUInt().toLong())
        .digestInt()

    /** The link as sent. */
    val snapshot: LinkSnapshot get() = LinkSnapshot(lower, higher)

    override fun equals(other: Any?): Boolean = other is LinkKey && other.lower == lower && other.higher == higher

    override fun hashCode(): Int = sampleScore

    companion object {
        private val HASH = XxHash32()

        /** The link between two nodes, in either order; `null` if they are the same node. */
        fun of(first: Int, second: Int): LinkKey? = when {
            first == second -> null
            else -> LinkKey(minOf(first, second), maxOf(first, second))
        }
    }
}
