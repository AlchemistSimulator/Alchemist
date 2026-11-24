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
 * A node in a [SimulationSnapshot].
 *
 * @property id the node id.
 * @property coordinates the node position.
 * @property values the values asked for, such as the concentration of a molecule, by name.
 * @property details what an inspector shows of the node, or `null` if not asked for, as it is costly.
 */
@Serializable
data class NodeSnapshot(
    val id: Int,
    val coordinates: List<Double>,
    val values: Map<String, ValueSnapshot> = emptyMap(),
    val details: NodeDetails? = null,
)
