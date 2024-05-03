/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations.queries

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLSimulationSurrogate
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Simple environment test queries.
 *
 * @param environment
 */
class EnvironmentQueries<T, P : Position<out P>>(private val environment: Environment<T, P>) : Query {

    /**
     * Returns the actual state of the simulation.
     */
    fun simulation() = environment.simulation.toGraphQLSimulationSurrogate()

    /**
     * Returns the actual state of the environment.
     */
    @GraphQLDescription("The actual state of the environment")
    fun environment() = this.environment.toGraphQLEnvironmentSurrogate()
}
