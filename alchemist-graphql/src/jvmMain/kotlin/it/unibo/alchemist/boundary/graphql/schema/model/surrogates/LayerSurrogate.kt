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
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position

/**
 * A surrogate class for [Layer]. A position mapping function resolves the translation
 * of a generic position (hopefully a
 * [it.unibo.alchemist.boundary.graphql.schema.model.surrogates.PositionSurrogate]) object
 * in a [PositionSurrogate].
 *
 * @param T the concentration type
 * @param P the position
 * @param posMapping the position mapping function from a generic set of coordinates
 * represented as a list of numbers, to an instance of Alchemist [Position] object
 */
@GraphQLDescription("A layer containing a substance or a molecule with a spatial distribution")
data class LayerSurrogate<T, P : Position<out P>>(
    @GraphQLIgnore override val origin: Layer<T, P>,
    @GraphQLIgnore val posMapping: (List<Number>) -> P,
) : GraphQLSurrogate<Layer<T, P>>(origin) {
    /**
     * Returns the value of the layer at the given position.
     *
     * @param p the position
     * @return the value of the layer at the given position, encoded
     * as a Json string (see [encodeConcentrationContentToString] for details)
     */
    @GraphQLDescription("Returns the value of the layer at the given position")
    fun getValue(p: PositionInput): String {
        val content = origin.getValue(posMapping(p.coordinates))
        return encodeConcentrationContentToString(content)
    }
}

/**
 * Converts a [it.unibo.alchemist.model.Layer] to a [LayerSurrogate].
 *
 * @param posMapping the position mapping function.
 * @return a [LayerSurrogate] for this [it.unibo.alchemist.model.Layer]
 */
fun <T, P : Position<out P>> Layer<T, P>.toGraphQLLayerSurrogate(posMapping: (List<Number>) -> P) =
    LayerSurrogate(this, posMapping)
