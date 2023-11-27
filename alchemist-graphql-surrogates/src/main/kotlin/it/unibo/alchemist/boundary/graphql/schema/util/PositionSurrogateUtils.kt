/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.util

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.GenericPositionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.Position2DSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.PositionInput
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.PositionSurrogate
import it.unibo.alchemist.model.Position

/**
 * Utility class for [PositionSurrogate] objects, used for conversion from [Position]
 * and [PositionInput] objects.
 */
object PositionSurrogateUtils {
    /**
     * Converts a [Position] to its relative [PositionSurrogate].
     *
     * @param position the [Position] to convert.
     * @return the [PositionSurrogate] relative to the given position.
     */
    @GraphQLIgnore
    fun toPositionSurrogate(position: Position<*>) = when (position.dimensions) {
        2 -> Position2DSurrogate(position.coordinates[0], position.coordinates[1])
        else -> GenericPositionSurrogate(position.coordinates.toList(), position.dimensions)
    }

    /**
     * Converts a [PositionInput] to its relative [PositionSurrogate].
     *
     * @param input the [PositionInput] to convert.
     * @return the [PositionSurrogate] relative to the given position.
     */
    @GraphQLIgnore
    fun fromPositionInput(input: PositionInput) = when (input.dimensions) {
        2 -> Position2DSurrogate(input.coordinates[0], input.coordinates[1])
        else -> GenericPositionSurrogate(input.coordinates, input.dimensions)
    }
}
