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
 * A constant [Layer] whose value is [baseLevel] plus [baselevel]:
 * the names of its parameters differ only in case, so they cannot be told apart when passed by name.
 */
class TwinLevelsLayer @JvmOverloads constructor(baseLevel: Level, baselevel: Level = Level(0.0)) :
    Layer<Double, Euclidean2DPosition> by ConstantLayer(baseLevel.value + baselevel.value)
