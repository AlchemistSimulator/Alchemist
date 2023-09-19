/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.launchers

import io.ktor.server.netty.EngineMain
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.core.Simulation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A launcher that starts a GraphQL server for exposing the simulation.
 */
class GraphQLServerLauncher : SimulationLauncher() {
    override fun launch(loader: Loader) {
        val simulation: Simulation<Any, Nothing> = prepareSimulation(loader, emptyMap<String, Any>())
        startServer(simulation)
    }

    private fun startServer(
        simulation: Simulation<Any, Nothing>,
        serverDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        return runBlocking {
            launch(serverDispatcher) {
                EngineMain.main(emptyArray())
            }
            simulation.run()
        }
    }
}
