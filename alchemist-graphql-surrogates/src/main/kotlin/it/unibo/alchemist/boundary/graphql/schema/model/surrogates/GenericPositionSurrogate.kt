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
 * An implementation of [PositionSurrogate] for a generic [Position] object.
 */
@GraphQLDescription("A generic position in space")
data class GenericPositionSurrogate(
    override val coordinates: List<Double>,
    override val dimensions: Int = coordinates.size,
) : PositionSurrogate
