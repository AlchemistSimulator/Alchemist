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
import it.unibo.alchemist.boundary.graphql.schema.util.encodeConcentrationContentToString
import it.unibo.alchemist.model.Concentration

/**
 * A GraphQL surrogate for a [Concentration] object.
 * **Note**: the content of the surrogate is a String (Json String) representation of the actual content inside the
 * [origin].
 * @param T The type of the original concentration.
 */
@GraphQLDescription("The concentration of a molecule, represented as a Json String or its string representation.")
data class ConcentrationSurrogate<T>(
    @GraphQLIgnore override val origin: Concentration<T>,
    /**
     * The content of the concentration of type [T], represented as a Json String.
     */
    val content: String,
) : GraphQLSurrogate<Concentration<T>>(origin)

/**
 * Converts a [Concentration] to a [ConcentrationSurrogate].
 * @return a [ConcentrationSurrogate] for this [Concentration], storing the Json String representation of the
 * content, or its String representation if the Json serialization fails.
 */
fun <T : Any> Concentration<T>.toGraphQLConcentrationSurrogate() =
    ConcentrationSurrogate(this, encodeConcentrationContentToString(this))
