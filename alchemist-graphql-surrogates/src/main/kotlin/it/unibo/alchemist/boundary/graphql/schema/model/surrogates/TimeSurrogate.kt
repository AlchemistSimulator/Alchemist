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
import it.unibo.alchemist.model.Time

/**
 * A GraphQL surrogate for a [Time].
 */
@GraphQLDescription("A time representation")
data class TimeSurrogate(
    @GraphQLIgnore override val origin: Time,
) : GraphQLSurrogate<Time>(origin) {

    /**
     * The time represented as a double.
     */
    @GraphQLDescription("The time represented as a double")
    val doubleTime: Double
        get() = origin.toDouble()
}

/**
 * Converts a [Time] into a [TimeSurrogate].
 * @return a [TimeSurrogate] representing the given [Time].
 */
fun Time.toGraphQLTimeSurrogate(): TimeSurrogate = TimeSurrogate(this)
