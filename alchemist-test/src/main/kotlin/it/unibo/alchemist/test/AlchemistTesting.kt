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

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.terminators.StepCount
import org.kaikikm.threadresloader.ResourceLoader

object AlchemistTesting {
    /**
     * Builds an empty environment, building a simulation [Engine], and binding them.
     * Optionally, some OutputMonitors can be provided.
     */
    @JvmStatic
    fun <T> createEmptyEnvironment(
        vararg outputMonitors: OutputMonitor<T, Euclidean2DPosition> = emptyArray(),
    ): Continuous2DEnvironment<T> {
        val incarnationName = SupportedIncarnations.getAvailableIncarnations().first()
        val incarnation = SupportedIncarnations.get<T, Euclidean2DPosition>(incarnationName).orElseThrow()
        val environment = Continuous2DEnvironment<T>(incarnation)
        val engine = Engine(environment)
        outputMonitors.forEach { engine.addOutputMonitor(it) }
        return environment
    }

    @JvmStatic
    fun <T, P : Position<P>> Simulation<T, P>.terminatingAfterSteps(finalStep: Long): Simulation<T, P> =
        apply { environment.addTerminator(StepCount(finalStep)) }

    /**
     * Prepares a [Simulation] given a [simulationResource] and, optionally, the [variables]' bindings.
     */
    @JvmStatic
    fun <T, P : Position<P>> loadAlchemist(
        simulationResource: String,
        variables: Map<String, *> = emptyMap<String, Nothing>(),
    ): Simulation<T, P> = loadAlchemistFromResource(simulationResource).getWith(variables)

    /**
     * Prepares a [Loader] given a [simulationResource].
     */
    @JvmStatic
    fun loadAlchemistFromResource(simulationResource: String): Loader {
        val file =
            requireNotNull(ResourceLoader.getResource(simulationResource)) {
                "$simulationResource could not be found in the classpath"
            }
        return LoadAlchemist.from(file)
    }

    /**
     * Runs an existing [Simulation] in the current thread.
     */
    @JvmStatic
    fun <T, P : Position<P>> Simulation<T, P>.runInCurrentThread(): Simulation<T, P> {
        play()
        run()
        return this
    }
}
