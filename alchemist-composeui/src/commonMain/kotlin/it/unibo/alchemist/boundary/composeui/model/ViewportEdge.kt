/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

import androidx.compose.runtime.Immutable

/**
 * An undirected edge projected in the central viewport.
 *
 * @property fromNodeId one end.
 * @property toNodeId the other end.
 */
@Immutable
internal data class ViewportEdge(val fromNodeId: Int, val toNodeId: Int) {
    init {
        require(fromNodeId != toNodeId) {
            "Viewport edges require two distinct endpoints."
        }
    }
}
