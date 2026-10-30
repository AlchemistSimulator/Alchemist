/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package another.location

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.layers.ConstantLayer
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A constant [Layer] whose value is [level] times [scale], plus [offset]:
 * its first parameter is an object, followed by optional parameters.
 */
class ScaledLevelLayer @JvmOverloads constructor(level: Level, scale: Double = 1.0, offset: Level = Level(0.0)) :
    Layer<Double, Euclidean2DPosition> by ConstantLayer(level.value * scale + offset.value)

/**
 * A level of a [ScaledLevelLayer].
 */
data class Level(val value: Double)
