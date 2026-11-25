/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import kotlinx.serialization.Serializable

/**
 * What a snapshot holds besides time, status and node positions, so that it costs only what its consumer needs.
 *
 * @property values the molecules whose concentration every node carries in [NodeSnapshot.values].
 * @property detailed the ids of the nodes whose [NodeDetails] are built.
 * @property maxLinks how many links are sent at most, a stable sample if there are more; `null` sends none.
 * @property valueLimits how much of each value is kept.
 * @property environmentReactions whether the reactions of the environment itself are sent.
 */
@Serializable
data class SnapshotRequest(
    val values: Set<String> = emptySet(),
    val detailed: Set<Int> = emptySet(),
    val maxLinks: Int? = null,
    val valueLimits: ValueLimits = ValueLimits(),
    val environmentReactions: Boolean = false,
) {
    init {
        require(maxLinks == null || maxLinks >= 0) { "The links sent cannot be negative: $maxLinks" }
    }
}
