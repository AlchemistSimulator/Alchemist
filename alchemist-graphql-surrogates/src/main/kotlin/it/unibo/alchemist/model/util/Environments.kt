/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.util

import it.unibo.alchemist.boundary.graphql.monitor.EnvironmentSubscriptionMonitor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Extension object for [Environment]s providing a function to retrieve the
 * [EnvironmentSubscriptionMonitor] of this [Environment].
 */
object Environments {
    /**
     * Returns the [EnvironmentSubscriptionMonitor] of this [Environment].
     * @return the [EnvironmentSubscriptionMonitor] of this [Environment]
     */
    fun <T, P> Environment<T, P>.subscriptionMonitor(): EnvironmentSubscriptionMonitor<T, P>
        where P : Position<out P> =
        this.simulation.outputMonitors.filterIsInstance<EnvironmentSubscriptionMonitor<T, P>>()
            .apply { check(size == 1) { "Only one subscription monitor is allowed" } }
            .first()
}
