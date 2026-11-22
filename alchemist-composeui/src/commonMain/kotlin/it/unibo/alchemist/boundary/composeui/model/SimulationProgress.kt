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
 * State for the bottom control dock progress section.
 *
 * @property fraction the completed fraction, from 0 to 1; `null` if unknown.
 * @property label the progress text.
 */
@Immutable
internal data class SimulationProgress(val fraction: Float? = null, val label: String = "Progress unavailable") {
    init {
        require(fraction == null || fraction in 0f..1f) {
            "Progress fraction must be null or within [0, 1]."
        }
    }
}
