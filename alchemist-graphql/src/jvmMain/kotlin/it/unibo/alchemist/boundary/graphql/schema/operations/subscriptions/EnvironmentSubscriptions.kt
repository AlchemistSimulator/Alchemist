/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations.subscriptions

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Subscription
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.EnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.operations.subscriptions.util.SubscriptionsUtils
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.flow.Flow

/**
 * Exposes alchemist [it.unibo.alchemist.model.Environment] as a GraphQL subscription
 * through [it.unibo.alchemist.boundary.graphql.schema.model.surrogates.EnvironmentSurrogate].
 */
class EnvironmentSubscriptions<T, P : Position<out P>>(environment: Environment<T, P>) : Subscription {

    private val environmentMonitor = SubscriptionsUtils.environmentSubscriptionMonitor(environment)

    /**
     * Returns a [Flow] with the updated value of the
     * [it.unibo.alchemist.boundary.graphql.schema.model.surrogates.EnvironmentSurrogate].
     */
    @GraphQLDescription("The simulation's environment")
    fun environment(): Flow<EnvironmentSurrogate<T, P>> = environmentMonitor.eventFlow
}
