/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Surrogate class for the [it.unibo.alchemist.model.interfaces.Position2D] interface.
 * @param x the first coordinate of the position.
 * @param y the second coordinate of the position.
 * @param coordinates the coordinates of the [PositionSurrogate], default using x and y.
 * @param dimensions the dimensions of the [PositionSurrogate], default to 2.
 */
@Serializable
@SerialName("Position2D")
data class Position2DSurrogate(
    val x: Double,
    val y: Double,
    override val coordinates: DoubleArray = doubleArrayOf(x, y),
    override val dimensions: Int = 2
) : PositionSurrogate {

    /**
     * Override equals because a [DoubleArray] is present in the properties.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Position2DSurrogate

        if (x != other.x) return false
        if (y != other.y) return false
        if (!coordinates.contentEquals(other.coordinates)) return false
        if (dimensions != other.dimensions) return false

        return true
    }

    /**
     * Override hashCode because a [DoubleArray] is present in the properties.
     */
    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + coordinates.contentHashCode()
        result = 31 * result + dimensions
        return result
    }
}
