/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.launch

import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.loader.Loader
import java.awt.GraphicsEnvironment
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Executes simulations locally in a headless environment.
 */
object HeadlessSimulationLauncher : SimulationLauncher() {

    override fun additionalValidation(currentOptions: AlchemistExecutionOptions) = with(currentOptions) {
        when {
            headless || GraphicsEnvironment.isHeadless() -> Validation.OK()
            graphics != null ->
                Validation.OK(
                    Priority.Fallback(
                        """
                        Graphical interface required but unavailable.
                        Import an Alchemist module with a graphical launcher, e.g. alchemist-swingui.
                        See: https://alchemistsimulator.github.io/wiki/usage/gui/
                        """.trimIndent()
                    )
                )
            distributed != null ->
                Validation.OK(
                    Priority.Fallback(
                        """
                        Distributed execution required but unavailable.
                        Import an Alchemist module with a distributed executor, e.g. alchemist-grid.
                        See: https://alchemistsimulator.github.io/wiki/usage/grid/
                        """.trimIndent()
                    )
                )
            else ->
                Validation.OK(
                    Priority.Fallback(
                        "Headless mode not explicitly requested, but no graphic environment found"
                    )
                )
        }
    }

    override fun launch(loader: Loader, parameters: AlchemistExecutionOptions) {
        var count = 0
        val executor = Executors.newFixedThreadPool(parameters.parallelism) {
            Thread(it).apply { name = "alchemist-executor-${count++}" }
        }
        val errorQueue = ConcurrentLinkedQueue<Throwable>()
        loader.variables
            .cartesianProductOf(parameters.variables)
            .forEach { variables ->
                executor.submit {
                    val simulation: Simulation<Any, Nothing> = prepareSimulation(loader, parameters, variables)
                    simulation.play()
                    simulation.run()
                    simulation.error.ifPresent {
                        errorQueue.add(it)
                        executor.shutdownNow()
                    }
                }
            }
        executor.shutdown()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
        if (errorQueue.isNotEmpty()) {
            throw errorQueue.reduce { previous, other ->
                previous.addSuppressed(other)
                previous
            }
        }
    }

    override val name = "Alchemist headless runner"
}
