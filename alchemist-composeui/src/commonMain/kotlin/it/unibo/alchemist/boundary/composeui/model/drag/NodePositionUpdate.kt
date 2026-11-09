/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model.drag

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * Final node coordinates produced by a viewport drag interaction.
 *
 * @property nodeId the node id.
 * @property coordinates the new position.
 */
@Immutable
data class NodePositionUpdate(val nodeId: Int, val coordinates: ImmutableList<Double>) {
    init {
        require(coordinates.size >= 2) {
            "Moved nodes require at least two coordinates."
        }
    }
}
