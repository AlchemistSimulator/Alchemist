/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.launch

import io.ktor.server.netty.EngineMain
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.webui.server.monitor.EnvironmentMonitorFactory.makeEnvironmentMonitor
import it.unibo.alchemist.boundary.webui.server.state.ServerStore.store
import it.unibo.alchemist.boundary.webui.server.state.actions.SetSimulation
import it.unibo.alchemist.core.Simulation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A launcher that starts a REST server to allow the visualization of the simulation on a Browser.
 */
class WebRendererLauncher : SimulationLauncher() {

    /**
     *  Prepares the simulation to be run, execute it in a coroutine and start the REST server by
     *  executing [EngineMain] using the application.conf configuration file.
     *  @param loader the loader of the simulation.
     *  @param parameters the parameters of the simulation.
     */
    override fun launch(loader: Loader) {
        val simulation: Simulation<Any, Nothing> = prepareSimulation(loader, emptyMap<String, Any>())
        store.dispatch(SetSimulation(simulation))
        simulation.addOutputMonitor(makeEnvironmentMonitor(simulation.environment))
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
