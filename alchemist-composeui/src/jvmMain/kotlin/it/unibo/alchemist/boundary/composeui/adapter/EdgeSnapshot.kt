/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.view.viewport.FULL_EDGE_RENDER_LIMIT
import it.unibo.alchemist.boundary.composeui.view.viewport.MAX_DRAWN_EDGES_PER_FRAME
import it.unibo.alchemist.boundary.composeui.view.viewport.SAMPLED_EDGE_RENDER_LIMIT
import java.util.Locale
import java.util.PriorityQueue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * The links to draw, and how they are drawn.
 *
 * @property edges the links to draw.
 * @property edgeCount the total number of links, drawn or not.
 * @property renderMode whether links are all drawn, sampled or hidden.
 * @property notice why links are sampled or hidden, if they are.
 */
internal data class EdgeSnapshot(
    val edges: ImmutableList<ViewportEdge> = persistentListOf(),
    val edgeCount: Int = 0,
    val renderMode: LinkRenderMode = LinkRenderMode.FULL,
    val notice: String? = null,
) {
    companion object {
        /**
         * The links among [edgePairs], ignoring direction, duplicates and self-links:
         * all of them if they are few, a stable sample if they are many, none if they are too many.
         */
        fun of(edgePairs: Sequence<Pair<Int, Int>>): EdgeSnapshot {
            val keys = edgePairs
                .mapNotNull { (first, second) -> EdgeKey.of(first, second) }
                .distinct()
                .take(SAMPLED_EDGE_RENDER_LIMIT + 1)
                .toList()
            return when {
                keys.size > SAMPLED_EDGE_RENDER_LIMIT -> EdgeSnapshot(
                    edgeCount = keys.size,
                    renderMode = LinkRenderMode.HIDDEN,
                    notice = "links hidden above ${SAMPLED_EDGE_RENDER_LIMIT.toReadableCount()}",
                )
                keys.size <= FULL_EDGE_RENDER_LIMIT -> EdgeSnapshot(
                    edges = keys.map(EdgeKey::edge).toImmutableList(),
                    edgeCount = keys.size,
                    renderMode = LinkRenderMode.FULL,
                )
                else -> EdgeSnapshot(
                    edges = sample(keys),
                    edgeCount = keys.size,
                    renderMode = LinkRenderMode.SAMPLED,
                    notice = "showing ${MAX_DRAWN_EDGES_PER_FRAME.toReadableCount()} sampled links",
                )
            }
        }

        /** The [MAX_DRAWN_EDGES_PER_FRAME] links with the lowest sample score, without sorting them all. */
        private fun sample(keys: List<EdgeKey>): ImmutableList<ViewportEdge> {
            val lowest = PriorityQueue(MAX_DRAWN_EDGES_PER_FRAME + 1, compareByDescending(EdgeKey::sampleScore))
            keys.forEach { key ->
                lowest.add(key)
                if (lowest.size > MAX_DRAWN_EDGES_PER_FRAME) {
                    lowest.poll()
                }
            }
            return lowest.sortedBy(EdgeKey::sampleScore).map(EdgeKey::edge).toImmutableList()
        }

        private fun Int.toReadableCount(): String = "%,d".format(Locale.ROOT, this)
    }
}
