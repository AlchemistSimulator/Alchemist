/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.states

import components.content.SimulationContext.Companion.DEFAULT_SCALE

data class ScaleTranslateState(
    var scale: Double = DEFAULT_SCALE.toDouble(),
    var translate: Pair<Double, Double> = Pair(0.0, 0.0),
)
