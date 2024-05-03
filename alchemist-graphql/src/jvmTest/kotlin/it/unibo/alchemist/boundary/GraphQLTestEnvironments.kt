/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import it.unibo.alchemist.boundary.graphql.monitor.EnvironmentSubscriptionMonitor
import it.unibo.alchemist.boundary.monitors.GraphQLMonitor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.util.ClassPathScanner

object GraphQLTestEnvironments {
    fun <T, P : Position<P>>loadTests(test: (Environment<T, P>) -> Unit) =
        ClassPathScanner.resourcesMatching(".*\\.yml", "it.unibo.alchemist.test.graphql")
            .asSequence()
            .map { LoadAlchemist.from(it) }
            .map { it.getDefault<T, P>() }
            .forEach { simulation ->
                simulation.outputMonitors.find { it is GraphQLMonitor<*, *> }
                simulation.outputMonitors.find { it is EnvironmentSubscriptionMonitor<*, *> }
                test(simulation.environment)
            }
}
