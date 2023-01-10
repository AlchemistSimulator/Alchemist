/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.monitor

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.model.SAPEREIncarnation
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.server.surrogates.utility.ToConcentrationSurrogate.toEmptyConcentration
import it.unibo.alchemist.server.surrogates.utility.ToPositionSurrogate.toSuitablePositionSurrogate

/**
 * A factory for [EnvironmentMonitor]s. Monitors are returned as [OutputMonitor].
 */
object EnvironmentMonitorFactory {

    /**
     * Create an EnvironmentMonitor suitable for the given simulation.
     * @param environment the environment of the simulation.
     * @return the [OutputMonitor].
     */
    fun makeEnvironmentMonitor(environment: Environment<*, *>): OutputMonitor<Any, Nothing> =
        makeEnvironmentMonitor(environment.incarnation, environment.dimensions)

    /**
     * Create an EnvironmentMonitor suitable for the given simulation, mapping the concentration based on the
     * [it.unibo.alchemist.model.interfaces.Incarnation] and mapping the [it.unibo.alchemist.model.interfaces.Position]
     * to the correct [it.unibo.alchemist.common.model.surrogate.PositionSurrogate] using the environment dimensions.
     * @param incarnation the [it.unibo.alchemist.model.interfaces.Incarnation] of the simulation.
     * @param dimensions the number of dimensions of the simulation.
     * @return the [OutputMonitor].
     */
    fun makeEnvironmentMonitor(incarnation: Incarnation<*, *>, dimensions: Int = 2): OutputMonitor<Any, Nothing> =
        EnvironmentMonitor(
            when (incarnation) {
                is SAPEREIncarnation<*> -> toEmptyConcentration // TODO change to correct implementation
                else -> toEmptyConcentration
            },
            toSuitablePositionSurrogate(dimensions)
        )
}
