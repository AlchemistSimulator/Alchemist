/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.util

import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportWorldBounds

/**
 * Geometric helpers over collections of [ViewportNode]s.
 */
internal object ViewportNodes {
    /**
     * Returns the smallest box containing the planar coordinates of these nodes, or null if there are no nodes.
     */
    fun List<ViewportNode>.toWorldBounds(): ViewportWorldBounds? = takeIf { isNotEmpty() }?.let { nodes ->
        ViewportWorldBounds(
            minX = nodes.minOf { it.x },
            maxX = nodes.maxOf { it.x },
            minY = nodes.minOf { it.y },
            maxY = nodes.maxOf { it.y },
        )
    }

    private val ViewportNode.x: Double get() = coordinates[0]

    private val ViewportNode.y: Double get() = coordinates[1]
}
