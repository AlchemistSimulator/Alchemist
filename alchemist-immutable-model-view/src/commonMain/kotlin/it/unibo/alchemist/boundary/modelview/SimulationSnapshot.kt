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
 * A running simulation at one moment, as an immutable description: no simulation can be rebuilt from it.
 * It holds only what was asked for when it was taken: whatever was not asked is `null`, never empty.
 *
 * @property time the simulation time.
 * @property step the number of steps done.
 * @property status the simulation status.
 * @property dimensions how many coordinates each position has.
 * @property nodes every node, with its position.
 * @property links the links between nodes, or `null` if not asked for.
 * @property environmentReactions the reactions of the environment itself, or `null` if not asked for.
 */
@Serializable
data class SimulationSnapshot(
    val time: Double = 0.0,
    val step: Long = 0,
    val status: SimulationStatus = SimulationStatus.INIT,
    val dimensions: Int = 2,
    val nodes: List<NodeSnapshot> = emptyList(),
    val links: LinksSnapshot? = null,
    val environmentReactions: List<ReactionSnapshot>? = null,
)
