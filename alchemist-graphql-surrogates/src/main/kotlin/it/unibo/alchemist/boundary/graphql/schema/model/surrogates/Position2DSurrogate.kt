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
import it.unibo.alchemist.model.Position

/**
 * A 2D surrogate for a [Position] object.
 *
 * @param x the x coordinate of the position.
 * @param y the y coordinate of the position.
 */
@GraphQLDescription("A position in a two dimensional space")
data class Position2DSurrogate(
    val x: Double,
    val y: Double,
    override val coordinates: List<Double> = listOf(x, y),
    override val dimensions: Int = 2,
) : PositionSurrogate
