/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.launchers

import it.unibo.alchemist.boundary.Loader
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Executes simulations locally in a headless environment.
 */
class HeadlessSimulationLauncher @JvmOverloads constructor(
    private val variables: List<String> = emptyList(),
    private val parallelism: Int = defaultParallelism,
) : SimulationLauncher() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun launch(loader: Loader) {
        var count = 0
        val executor = Executors.newFixedThreadPool(parallelism) {
            Thread(it).apply { name = "alchemist-executor-${count++}" }
        }
        val errorQueue = ConcurrentLinkedQueue<Throwable>()
        val cartesianProduct = loader.variables.cartesianProductOf(variables)
        cartesianProduct.forEach { newVariables ->
            executor.submit {
                runCatching { prepareSimulation<Any, Nothing>(loader, newVariables) }
                    .onFailure { logger.error("Error during the preparation of the simulation: $newVariables", it) }
                    .mapCatching { simulation ->
                        simulation.play()
                        simulation.run()
                        simulation.error.ifPresent { throw it }
                        logger.info("Simulation with {} completed successfully", newVariables)
                    }
                    .onFailure {
                        logger.error("Failure in Simulation with $newVariables", it)
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
}
