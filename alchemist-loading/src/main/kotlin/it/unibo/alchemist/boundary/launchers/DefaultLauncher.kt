/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.launchers

import com.google.common.collect.Lists
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.util.BugReporting
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * The default launcher for simulations.
 * If [batch] variables are specified, the simulation is run once for each combination of their values.
 * If [autoStart] is true (default), the simulation is started automatically.
 * If [showProgress] is true (default), a message is printed to the console every time a simulation completes.
 * If [parallelism] is greater than 1, the simulations are run in parallel;
 * defaults to the number of logical cores detected by the JVM.
 */
open class DefaultLauncher
@JvmOverloads
constructor(
    val batch: List<String> = emptyList(),
    val autoStart: Boolean = true,
    val showProgress: Boolean = true,
    val parallelism: Int = Runtime.getRuntime().availableProcessors(),
) : Launcher {
    @JvmOverloads
    constructor(
        autoStart: Boolean,
        showProgress: Boolean = true,
        parallelism: Int = Runtime.getRuntime().availableProcessors(),
    ) : this(emptyList(), autoStart, showProgress, parallelism)

    /**
     * Launches a simulation using the provided [loader].
     */
    @Synchronized
    override fun launch(loader: Loader) {
        fun Simulation<*, *>.configured() =
            apply {
                if (autoStart) {
                    play()
                }
            }
        val instances = loader.variables.cartesianProductOf(batch)
        if (instances.isEmpty()) {
            BugReporting.reportBug(
                "No simulation instances were created",
                mapOf(
                    "requested batch" to batch,
                    "variables" to loader.variables,
                ),
            )
        }
        val launchId = launchId.getAndIncrement()
        when {
            instances.size == 1 ->
                loader.getWith<Any?, Nothing>(instances.first()).configured().run()
            parallelism == 1 ->
                instances.forEach {
                    loader.getWith<Any?, Nothing>(it).configured().run()
                }
            else -> {
                val workerId = AtomicInteger(0)
                val executor =
                    Executors.newFixedThreadPool(parallelism) {
                        Thread(it, "Alchemist Pool $launchId worker ${workerId.getAndIncrement()}")
                    }
                val errorQueue = ConcurrentLinkedDeque<Throwable>()
                instances.forEachIndexed { index, instance ->
                    executor.submit {
                        runCatching { loader.getWith<Any?, Nothing>(instance).configured() }
                            .mapCatching { simulation ->
                                simulation.run()
                                simulation.error.ifPresent { throw it }
                                logger.info("Simulation with {} completed successfully", instance)
                            }
                            .onFailure {
                                logger.error("Failure for simulation with $instance", it)
                                errorQueue.add(it)
                                executor.shutdownNow()
                            }
                            .onSuccess {
                                if (showProgress) {
                                    logger.info("Simulation {} of {} completed", index + 1, instances.size)
                                }
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
    }

    protected companion object {
        /**
         * If no specific number of parallel threads to use is specified, this value is used.
         * Defaults to the number of logical cores detected by the JVM.
         */
        @JvmStatic
        protected val defaultParallelism = Runtime.getRuntime().availableProcessors()

        private val launchId = AtomicInteger(0)

        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        protected fun Map<String, Variable<*>>.cartesianProductOf(
            variables: Collection<String>,
        ): List<Map<String, Serializable?>> {
            require(keys.containsAll(variables)) {
                "Variables ${variables - keys} are not defined. Valid values are: $this"
            }
            val variableValues =
                variables.map { variableName ->
                    this[variableName]
                        ?.map { variableName to it }
                        ?: BugReporting.reportBug(
                            "Variable was supposed to be available, but it is not",
                            mapOf(
                                "variableName" to variableName,
                                "requested variables" to variables,
                                "available variables" to this,
                            ),
                        )
                }.toList()
            return Lists.cartesianProduct(variableValues)
                .map { it.toMap() }
                .takeUnless { it.isEmpty() }
                ?: listOf(emptyMap())
        }
    }
}
