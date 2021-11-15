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
import it.unibo.alchemist.grid.cluster.ClusterImpl
import it.unibo.alchemist.grid.config.LocalGeneralSimulationConfig
import it.unibo.alchemist.grid.config.SimulationConfigImpl
import it.unibo.alchemist.grid.simulation.SimulationSetImpl
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.model.implementations.times.DoubleTime
import java.nio.file.Paths

/**
 * Launches a simulation set on a cluster of Alchemist nodes running in server mode.
 */
object DistributedExecution : SimulationLauncher() {

    override val name = "Alchemist execution on a grid system"

    override fun additionalValidation(currentOptions: AlchemistExecutionOptions) = with(currentOptions) {
        when {
            variables.isEmpty() -> Validation.Invalid("$name requires a variable set")
            distributed == null -> Validation.Invalid("No configuration file for distributed execution")
            graphics != null -> Validation.OK(Priority.Fallback("Distributed execution will ignore graphical settings"))
            parallelism != AlchemistExecutionOptions.defaultParallelism -> incompatibleWith("custom parallelism")
            else -> Validation.OK()
        }
    }

    override fun launch(loader: Loader, parameters: AlchemistExecutionOptions) {
        val simulationConfig = LocalGeneralSimulationConfig(loader, DoubleTime(parameters.endTime))
        val simConfigs = loader.variables.cartesianProductOf(parameters.variables).map(::SimulationConfigImpl)
        val simulationSet = SimulationSetImpl(simulationConfig, simConfigs)
        val cluster = ClusterImpl(
            Paths.get(parameters.distributed ?: throw IllegalStateException("No remote configuration file "))
        )
        cluster.getWorkersSet(simulationSet.computeComplexity()).distributeSimulations(simulationSet)
    }
}
