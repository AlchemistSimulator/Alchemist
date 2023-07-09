/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.launch

import com.google.common.collect.Lists
import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.EngineMode
import it.unibo.alchemist.OutputReplayStrategy
import it.unibo.alchemist.core.implementations.ArrayIndexedPriorityEpsilonBatchQueue
import it.unibo.alchemist.core.implementations.ArrayIndexedPriorityFixedBatchQueue
import it.unibo.alchemist.core.implementations.BatchEngine
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.export.exporters.GlobalExporter
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Position
import org.kaikikm.threadresloader.ResourceLoader
import java.io.File
import java.io.Serializable

/**
 * A launcher stub for simulation execution.
 * Takes care of creating a [Loader],
 * and provides support functions for generating simulations and computing the possible parameters configurations.
 */
abstract class SimulationLauncher : AbstractLauncher() {

    final override fun validate(currentOptions: AlchemistExecutionOptions) = with(currentOptions) {
        when {
            configuration == null -> requires("a simulation file")
            help -> incompatibleWith("help printing")
            server != null -> incompatibleWith("Alchemist grid computing server mode")
            else -> additionalValidation(currentOptions)
        }
    }

    final override fun launch(parameters: AlchemistExecutionOptions) = with(parameters) {
        checkNotNull(configuration) { "Invalid configuration $configuration" }
        val loader = LoadAlchemist.from(
            ResourceLoader.getResource(configuration)
                ?: File(configuration).takeIf { it.exists() && it.isFile }?.toURI()?.toURL()
                ?: error("No classpath resource or file $configuration was found"),
        )
        launch(loader, parameters)
    }

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
        parameters: AlchemistExecutionOptions,
        variables: Map<String, *>,
    ): Simulation<T, P> {
        val initialized: InitializedEnvironment<T, P> = loader.getWith(variables)
        val simulation = buildEngine(initialized, parameters)
        if (initialized.exporters.isNotEmpty()) {
            simulation.addOutputMonitor(GlobalExporter(initialized.exporters))
        }
        return simulation
    }

    private fun <T, P : Position<P>> buildEngine(
        initialized: InitializedEnvironment<T, P>,
        parameters: AlchemistExecutionOptions,
    ): Engine<T, P> {
        val outputReplayStrategy = parameters.engineConfig.outputReplayStrategy
        return when (parameters.engineConfig.engineMode) {
            EngineMode.BATCH_FIXED -> {
                val batchSize = parameters.engineConfig.batchSize ?: parameters.parallelism
                BatchEngine(
                    initialized.environment,
                    Long.MAX_VALUE,
                    DoubleTime(parameters.endTime),
                    parameters.parallelism,
                    outputReplayStrategy.toEngine(),
                    ArrayIndexedPriorityFixedBatchQueue(batchSize),
                )
            }

            EngineMode.EPSILON -> {
                val epsilon = parameters.engineConfig.epsilon
                BatchEngine(
                    initialized.environment,
                    Long.MAX_VALUE,
                    DoubleTime(parameters.endTime),
                    parameters.parallelism,
                    outputReplayStrategy.toEngine(),
                    ArrayIndexedPriorityEpsilonBatchQueue(epsilon),
                )
            }

            else -> Engine(
                initialized.environment,
                DoubleTime(parameters.endTime),
            )
        }
    }

    private fun OutputReplayStrategy.toEngine(): BatchEngine.OutputReplayStrategy {
        return when (this) {
            OutputReplayStrategy.AGGREGATE -> BatchEngine.OutputReplayStrategy.AGGREGATE
            OutputReplayStrategy.REPLAY -> BatchEngine.OutputReplayStrategy.REPLAY
        }
    }

    /**
     * Launches a simulation using the provided [loader] and option [parameters].
     */
    abstract fun launch(loader: Loader, parameters: AlchemistExecutionOptions)

    /**
     * Allows subclasses to perform further checks before getting executed. Defaults to simply return [Validation.OK]
     */
    abstract fun additionalValidation(currentOptions: AlchemistExecutionOptions): Validation

    companion object {
        /**
         *  Default epsilon value for epsilon batch mode.
         */
        const val DEFAULT_EPSILON_VALUE = 0.01
    }
}
