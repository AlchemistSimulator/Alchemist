/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations.subscriptions.util

import it.unibo.alchemist.boundary.graphql.monitor.EnvironmentSubscriptionMonitor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Utility class for GraphQL subscriptions.
 */
object SubscriptionsUtils {

    /**
     * Returns the [EnvironmentSubscriptionMonitor] of the given [Environment].
     * @param environment the environment.
     */
    fun <T, P>environmentSubscriptionMonitor(environment: Environment<T, P>): EnvironmentSubscriptionMonitor<T, P>
        where P : Position<out P> =
        environment.simulation.outputMonitors.filterIsInstance<EnvironmentSubscriptionMonitor<T, P>>()
            .apply { check(size == 1) { "Only one subscription monitor is allowed" } }
            .first()
}
