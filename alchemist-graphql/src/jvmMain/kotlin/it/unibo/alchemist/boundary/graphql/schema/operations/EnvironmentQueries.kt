/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations

import com.expediagroup.graphql.server.operations.Query
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Simple environment test queries.
 *
 * @param env the simulation [Environment]
 */
class EnvironmentQueries<T, P : Position<out P>>(private val env: Environment<T, P>) : Query {
    private val envSurrogate = env.toGraphQLEnvironmentSurrogate()

    /**
     * Returns the actual state of the environment.
     */
    fun currentEnv() = this.envSurrogate
}
