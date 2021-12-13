/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JvmName("AlchemistTesting")
package it.unibo.alchemist.testsupport

import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition

/**
 * Builds an empty environment, building a simulation [Engine], and binding them.
 * Optionally, some OutputMonitors can be provided.
 */
fun <T> createEmptyEnvironment(
    vararg outputMonitors: OutputMonitor<T, Euclidean2DPosition> = emptyArray()
): Continuous2DEnvironment<T> {
    val incarnationName = SupportedIncarnations.getAvailableIncarnations().first()
    val incarnation = SupportedIncarnations.get<T, Euclidean2DPosition>(incarnationName).orElseThrow()
    val environment = Continuous2DEnvironment<T>(incarnation)
    val engine = Engine(environment)
    environment.simulation = engine
    outputMonitors.forEach { engine.addOutputMonitor(it) }
    return environment
}
