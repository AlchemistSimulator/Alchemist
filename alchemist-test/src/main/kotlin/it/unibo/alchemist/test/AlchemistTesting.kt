/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JvmName("AlchemistTesting")

package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.export.exporters.GlobalExporter
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.kaikikm.threadresloader.ResourceLoader

/**
 * Builds an empty environment, building a simulation [Engine], and binding them.
 * Optionally, some OutputMonitors can be provided.
 */
fun <T> createEmptyEnvironment(
    vararg outputMonitors: OutputMonitor<T, Euclidean2DPosition> = emptyArray(),
): Continuous2DEnvironment<T> {
    val incarnationName = SupportedIncarnations.getAvailableIncarnations().first()
    val incarnation = SupportedIncarnations.get<T, Euclidean2DPosition>(incarnationName).orElseThrow()
    val environment = Continuous2DEnvironment<T>(incarnation)
    val engine = Engine(environment)
    environment.simulation = engine
    outputMonitors.forEach { engine.addOutputMonitor(it) }
    return environment
}

/**
 * Prepares an [InitializedEnvironment] given a [simulationResource] and, optionally, the [variables]' bindings.
 */
fun <T, P : Position<P>> loadAlchemist(
    simulationResource: String,
    variables: Map<String, *> = emptyMap<String, Nothing>(),
): InitializedEnvironment<T, P> = loadAlchemistFromResource(simulationResource).getWith(variables)

/**
 * Prepares a [Loader] given a [simulationResource].
 */
fun loadAlchemistFromResource(simulationResource: String): Loader {
    val file = requireNotNull(ResourceLoader.getResource(simulationResource)) {
        "$simulationResource could not be found in the classpath"
    }
    return LoadAlchemist.from(file)
}

/**
 * Builds a new [Engine], adding a [GlobalExporter] with the required [it.unibo.alchemist.loader.export.Exporter]s.
 */
fun <T, P : Position<P>> InitializedEnvironment<T, P>.createSimulation(
    finalTime: Time = Time.INFINITY,
    finalStep: Long = Long.MAX_VALUE,
) = Engine(environment, finalStep, finalTime).apply { addOutputMonitor(GlobalExporter(exporters)) }

/**
 * Runs an existing [Simulation] in the current thread.
 */
fun <T, P : Position<P>> Simulation<T, P>.runInCurrentThread(): Simulation<T, P> {
    play()
    run()
    return this
}
