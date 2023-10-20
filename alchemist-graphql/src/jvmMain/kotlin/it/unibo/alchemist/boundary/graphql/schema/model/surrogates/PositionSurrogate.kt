/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model.surrogates

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.model.Position

/**
 * Surrogate for a generic [Position] objects.
 */
@GraphQLDescription("Generic position in space")
interface PositionSurrogate {
    /**
     * The coordinates of the position.
     */
    val coordinates: List<Double>

    /**
     * The dimension of the position.
     */
    val dimensions: Int

    /**
     * Converts this surrogate to a [PositionInput] object.
     * @return a [PositionInput] object.
     */
    @GraphQLIgnore
    fun toInputPosition() = PositionInput(coordinates, dimensions)
}

/**
 * GraphQL input object for a generic position, that will avoid
 * the use of a full [Position] or [PositionSurrogate] when
 * client executes operations that require a position as parameter.
 *
 * @param coordinates the coordinates of the position.
 * @param dimensions the dimension of the position.
 */
@GraphQLDescription("Position in space, used as input parameter")
data class PositionInput(
    val coordinates: List<Double>,
    val dimensions: Int = coordinates.size,
)
