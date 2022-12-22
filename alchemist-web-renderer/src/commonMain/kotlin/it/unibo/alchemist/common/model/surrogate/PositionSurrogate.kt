/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import kotlinx.serialization.Serializable

/**
 *  Surrogate interface for the [it.unibo.alchemist.model.interfaces.Position] interface.
 *  Note: by using the sealed keyword any subclass of [PositionSurrogate] can be serialized and deserialized
 *  automatically using a polymorphic serializer.
 */
@Serializable
sealed interface PositionSurrogate {
    /**
     * The coordinates of the position.
     */
    val coordinates: DoubleArray

    /**
     * The number of dimensions of the position.
     */
    val dimensions: Int
}
