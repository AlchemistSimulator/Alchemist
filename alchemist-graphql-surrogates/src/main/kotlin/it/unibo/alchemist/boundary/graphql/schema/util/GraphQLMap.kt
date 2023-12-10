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

/**
 * Generic GraphQL compliant representation of a [Map].
 *
 * @param originMap the wrapped map
 * @param size the size of the wrapped map
 */
@GraphQLIgnore
open class GraphQLMap<K, V>(
    open val originMap: Map<K, V>,
    open val size: Int,
) {
    /**
     * Custom indexing with the key of the wrapped [Map].
     */
    @GraphQLIgnore operator fun get(key: K) = originMap[key]
}
