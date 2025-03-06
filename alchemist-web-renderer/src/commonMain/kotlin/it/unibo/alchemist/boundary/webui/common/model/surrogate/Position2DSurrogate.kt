/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Surrogate class for the [it.unibo.alchemist.model.Position2D] interface.
 * @param x the first coordinate of the position.
 * @param y the second coordinate of the position.
 */
@Serializable
@SerialName("Position2D")
data class Position2DSurrogate(val x: Double, val y: Double) : PositionSurrogate {
    override val coordinates: DoubleArray = doubleArrayOf(x, y)
    override val dimensions: Int = 2
}
