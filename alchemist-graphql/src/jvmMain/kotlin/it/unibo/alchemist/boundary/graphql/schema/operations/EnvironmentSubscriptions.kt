/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations

import com.expediagroup.graphql.server.operations.Subscription
import it.unibo.alchemist.boundary.graphql.util.GraphQLSimulationContext
import it.unibo.alchemist.model.Position

/**
 * Exposes alchemist [it.unibo.alchemist.model.Environment] as a GraphQL subscription
 * through [it.unibo.alchemist.boundary.graphql.schema.model.surrogates.EnvironmentSurrogate].
 * @param simulationContext current simulation context as a [it.unibo.alchemist.boundary.graphql.server.attributes.GraphQLSimulationContext]
 */
class EnvironmentSubscriptions<T, P : Position<out P>>(
    private val simulationContext: GraphQLSimulationContext<T, P>,
) : Subscription {

    /**
     * Returns the actual state of the environment.
     */
    fun environment() = simulationContext.environmentEmitter.asFlow()
}
