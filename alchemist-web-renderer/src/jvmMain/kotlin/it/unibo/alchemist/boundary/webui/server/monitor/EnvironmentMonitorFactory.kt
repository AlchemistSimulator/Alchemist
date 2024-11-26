/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.monitor

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.ToConcentrationSurrogate.toEmptyConcentration
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.ToPositionSurrogate.toSuitablePositionSurrogate
import it.unibo.alchemist.model.Environment

/**
 * A factory for [EnvironmentMonitor]s. Monitors are returned as [OutputMonitor].
 */
object EnvironmentMonitorFactory {
    /**
     * Create an EnvironmentMonitor suitable for the given simulation, using a toConcentration function based on the
     * [it.unibo.alchemist.model.Incarnation]
     * and mapping the
     * [it.unibo.alchemist.model.Position]
     * to the correct
     * [it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate]
     * using the environment dimensions.
     *
     * @param environment the environment of the simulation.
     * @return the [OutputMonitor].
     */
    fun makeEnvironmentMonitor(environment: Environment<*, *>): OutputMonitor<Any, Nothing> =
        EnvironmentMonitor(
            toEmptyConcentration, // TODO change to correct implementation depending on the incarnation
            toSuitablePositionSurrogate(environment.dimensions),
        )
}
