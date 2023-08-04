/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.launch

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.grid.cluster.ClusterImpl
import it.unibo.alchemist.boundary.grid.config.LocalGeneralSimulationConfig
import it.unibo.alchemist.boundary.grid.config.SimulationConfigImpl
import it.unibo.alchemist.boundary.grid.simulation.SimulationSetImpl
import it.unibo.alchemist.model.Time
import java.nio.file.Paths

/**
 * Launches a simulation set on a cluster of Alchemist nodes running in server mode.
 */
class DistributedExecution(
    private val variables: List<String> = emptyList(),
    private val distributedConfigPath: String?,
) : SimulationLauncher() {

    override fun launch(loader: Loader) {
        val simulationConfig = LocalGeneralSimulationConfig(
            loader,
            Time.INFINITY,
        )
        val simConfigs = loader.variables.cartesianProductOf(variables).map(::SimulationConfigImpl)
        val simulationSet = SimulationSetImpl(
            simulationConfig,
            simConfigs,
        )
        val cluster =
            ClusterImpl(Paths.get(requireNotNull(distributedConfigPath) { "No remote configuration file" }))
        cluster.getWorkersSet(simulationSet.computeComplexity()).distributeSimulations(simulationSet)
    }
}
