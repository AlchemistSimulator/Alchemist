/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.states

import components.content.shared.CommonProperties.RenderProperties.DEFAULT_SCALE

/**
 * Represents the state of scaling and translation in the canvas context.
 * @property scale The scaling factor applied to the graphical elements. Defaults to DEFAULT_SCALE.
 * @property translate The translation offset applied to the graphical elements, represented as a pair of (x, y) coordinates. Defaults to (0.0, 0.0).
 * @constructor Creates a ScaleTranslateState with the specified scaling factor and translation offset, which default to DEFAULT_SCALE and (0.0, 0.0) respectively.
 */
data class ScaleTranslateState(
    var scale: Double = DEFAULT_SCALE,
    var translate: Pair<Double, Double> = Pair(0.0, 0.0),
)
