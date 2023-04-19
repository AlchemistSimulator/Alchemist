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
 *  Surrogate class for the [it.unibo.alchemist.model.interfaces.Position] interface.
 *  This implementation is valid for any type of position.
 *  @param coordinates the coordinates of the [PositionSurrogate].
 *  @param dimensions the dimensions of the [PositionSurrogate].
 */
@Serializable
@SerialName("Position")
data class GeneralPositionSurrogate(
    override val coordinates: DoubleArray,
    override val dimensions: Int,
) : PositionSurrogate {
    init {
        require(coordinates.size == dimensions) {
            "The number of coordinates must be equal to the number of dimensions."
        }
    }

    /**
     * Override equals because a [DoubleArray] is present in the properties.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PositionSurrogate

        if (!coordinates.contentEquals(other.coordinates)) return false
        if (dimensions != other.dimensions) return false

        return true
    }

    /**
     * Override hashCode because a [DoubleArray] is present in the properties.
     */
    override fun hashCode(): Int {
        var result = coordinates.contentHashCode()
        result = 31 * result + dimensions
        return result
    }
}
