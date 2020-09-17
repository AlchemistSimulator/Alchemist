/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.launch

import com.google.common.collect.Lists
import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.loader.export.Exporter
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import org.kaikikm.threadresloader.ResourceLoader
import java.io.File
import java.io.Serializable
import kotlin.streams.toList

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
        if (configuration == null) {
            throw IllegalStateException("Invalid configuration $configuration")
        }
        val loader = YamlLoader(
            ResourceLoader.getResource(configuration)?.openStream()
                ?: File(configuration).takeIf { it.exists() && it.isFile }?.inputStream()
                ?: throw IllegalStateException("No classpath resource or file $configuration was found")
        )
        launch(loader, parameters)
    }

    protected fun Map<String, Variable<*>>.cartesianProductOf(
        variables: Collection<String>
    ): List<Map<String, Serializable?>> {
        val variableValues = variables.map { variableName ->
            this[variableName]?.map { variableName to it }
                ?: throw IllegalArgumentException("$variableName does not exist in $this")
        }.toList()
        return Lists.cartesianProduct(variableValues).map { it.toMap() }.takeUnless { it.isEmpty() }
            ?: listOf(emptyMap())
    }

    protected fun <T, P : Position<P>> prepareSimulation(
        loader: Loader,
        parameters: AlchemistExecutionOptions,
        variables: Map<String, *>
    ): Simulation<T, P> {
        val environment: Environment<T, P> = loader.getWith(variables)
        val simulation = Engine(environment, DoubleTime(parameters.endTime))
        if (parameters.export != null) {
            val variablesDescriptor = variables
                .map { (name, value) -> "$name-$value" }
                .joinToString(separator = "_")
            val filename = "${parameters.export}${if (variables.isEmpty()) "" else "_"}$variablesDescriptor"
            val header = loader.variables
                .mapValues { (variableName, variable) -> variables[variableName] ?: variable.default }
                .map { (variableName, variableValue) -> "$variableName = $variableValue" }
                .joinToString()
            simulation.addOutputMonitor(Exporter(filename, parameters.interval, header, loader.dataExtractors))
        }
        return simulation
    }

    /**
     * Launches a simulation using the provided [loader] and option [parameters].
     */
    abstract fun launch(loader: Loader, parameters: AlchemistExecutionOptions)

    /**
     * Allows subclasses to perform further checks before getting executed. Defaults to simply return [Validation.OK]
     */
    abstract fun additionalValidation(currentOptions: AlchemistExecutionOptions): Validation
}
