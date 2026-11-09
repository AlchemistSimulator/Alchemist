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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * A node projected in the central viewport.
 *
 * @property id the node id.
 * @property coordinates the node position; only the first two are drawn.
 * @property accent the node color, from secondary (0) to primary (1) accent.
 * @property metadata extra details about the node.
 * @property concentrations the node contents, by molecule.
 */
@Immutable
data class ViewportNode(
    val id: Int,
    val coordinates: ImmutableList<Double>,
    val accent: Float = 0.5f,
    val metadata: ImmutableList<Labeled<Any?>> = persistentListOf(),
    val concentrations: ImmutableList<Labeled<Any?>> = persistentListOf(),
) {
    init {
        require(coordinates.size >= 2) {
            "Viewport nodes require at least two coordinates."
        }
    }
}
