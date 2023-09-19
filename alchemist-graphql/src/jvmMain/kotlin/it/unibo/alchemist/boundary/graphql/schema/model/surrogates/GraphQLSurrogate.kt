/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model.surrogates

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore

/**
 * A surrogate for a GraphQL object.
 * Each surrogate has an [origin] object, which is the alchemist object that the surrogate represents.
 * @param T The type of the alchemist object that the surrogate represents.
 */
open class GraphQLSurrogate<T>(
    @GraphQLIgnore open val origin: T,
)
