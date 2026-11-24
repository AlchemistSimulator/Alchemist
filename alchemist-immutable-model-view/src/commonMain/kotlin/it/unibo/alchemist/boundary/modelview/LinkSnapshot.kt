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
 * A link between the nodes [from] and [to].
 *
 * @property from the id of a node.
 * @property to the id of the other node.
 */
@Serializable
data class LinkSnapshot(
    val from: Int,
    val to: Int,
)
