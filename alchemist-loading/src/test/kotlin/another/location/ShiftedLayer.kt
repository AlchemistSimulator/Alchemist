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
 * A constant [Layer] whose value is [level] plus the amount of [shift].
 */
class ShiftedLayer(level: Double, shift: Shift) :
    Layer<Double, Euclidean2DPosition> by ConstantLayer(level + shift.amount)

/**
 * A shift of a [ShiftedLayer].
 */
interface Shift {
    /**
     * How much the level is shifted.
     */
    val amount: Double
}

/**
 * A [Shift] of one.
 */
object UnitShift : Shift {
    override val amount: Double = 1.0
}

/**
 * A [Layer] that is an object, always zero.
 */
object ZeroLayer : Layer<Double, Euclidean2DPosition> by ConstantLayer(0.0)
