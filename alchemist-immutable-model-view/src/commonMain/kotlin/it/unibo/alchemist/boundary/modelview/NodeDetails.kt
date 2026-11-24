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
 * Everything a node holds and runs.
 *
 * @property contents every molecule in the node, with its value.
 * @property reactions the reactions.
 * @property properties the properties, as text.
 */
@Serializable
data class NodeDetails(
    val contents: List<ContentSnapshot> = emptyList(),
    val reactions: List<ReactionSnapshot> = emptyList(),
    val properties: List<String> = emptyList(),
)
