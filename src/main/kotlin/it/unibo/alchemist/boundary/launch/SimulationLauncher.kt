/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.launch

import com.google.common.collect.Lists
import it.unibo.alchemist.boundary.InitializedEnvironment
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import java.io.Serializable

/**
 * A launcher stub for simulation execution.
 * Takes care of creating a [Loader],
 * and provides support functions for generating simulations and computing the possible parameters configurations.
 */
abstract class SimulationLauncher : Launcher {

    protected fun Map<String, Variable<*>>.cartesianProductOf(
        variables: Collection<String>,
    ): List<Map<String, Serializable?>> {
        val variableValues = variables.map { variableName ->
            this[variableName]
                ?.map { variableName to it }
                ?: throw IllegalArgumentException(
                    "$variableName does not exist among the variables. Valid values are: $this",
                )
        }.toList()
        return Lists.cartesianProduct(variableValues).map { it.toMap() }.takeUnless { it.isEmpty() }
            ?: listOf(emptyMap())
    }

    protected fun <T, P : Position<P>> prepareSimulation(
        loader: Loader,
        variables: Map<String, *>,
    ): Simulation<T, P> {
        val initialized: InitializedEnvironment<T, P> = loader.getWith(variables)
        val simulation = Engine(
            initialized.environment,
            Time.INFINITY,
        )
        if (initialized.exporters.isNotEmpty()) {
            simulation.addOutputMonitor(GlobalExporter(initialized.exporters))
        }
        return simulation
    }

    /**
     * Launches a simulation using the provided [loader] and option [parameters].
     */
    abstract override fun launch(loader: Loader)

    companion object {
        /**
         * If no specific number of parallel threads to use is specified, this value is used.
         * Defaults to the number of logical cores detected by the JVM.
         */
        @JvmStatic
        protected val defaultParallelism = Runtime.getRuntime().availableProcessors()
    }
}
