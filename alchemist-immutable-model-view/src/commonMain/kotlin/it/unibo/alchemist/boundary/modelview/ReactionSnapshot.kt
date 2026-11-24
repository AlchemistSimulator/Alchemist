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
 * A reaction of a node.
 *
 * @property type the kind of reaction, such as its class name.
 * @property nextOccurrence when it is due to run next; infinite if never.
 * @property executable whether it can currently run.
 * @property meanDelay the expected delay between two occurrences, or `null` if it has no time distribution.
 * @property timeDistribution its time distribution as text, or `null` if it has none.
 * @property conditions its conditions, as text.
 * @property actions its actions, as text.
 * @property text the reaction as it describes itself, including what the other fields do not hold, such as
 * how it is scheduled.
 */
@Serializable
data class ReactionSnapshot(
    val type: String,
    val nextOccurrence: Double,
    val executable: Boolean,
    val meanDelay: Double? = null,
    val timeDistribution: String? = null,
    val conditions: List<String> = emptyList(),
    val actions: List<String> = emptyList(),
    val text: String = type,
)
