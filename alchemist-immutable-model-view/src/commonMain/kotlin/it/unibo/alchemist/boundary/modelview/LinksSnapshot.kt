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
 * The links between nodes.
 *
 * @property links the links sent, possibly a sample of them.
 * @property total how many links there are, sent or not.
 */
@Serializable
data class LinksSnapshot(
    val links: List<LinkSnapshot> = emptyList(),
    val total: Int = links.size,
)
