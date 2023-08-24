/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter.launch

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.launchers.SimulationLauncher
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.multivesta.adapter.AlchemistMultiVesta
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Launches a single simulation run that can be controlled by MultiVesta.
 */
class AlchemistMultiVestaSimulationLauncher : SimulationLauncher() {

    private val logger = LoggerFactory.getLogger(AlchemistMultiVestaSimulationLauncher::class.java)

    /**
     * The simulation that was launched.
     */
    lateinit var simulation: Simulation<Any, Nothing>
        private set

    /**
     * Launch the simulation and pause it immediately before step 0.
     */
    override fun launch(loader: Loader) {
        simulation = prepareSimulation(loader, emptyMap<String, Any>())
        logger.info("Simulation prepared")
        simulation.goToStep(-1) // configure the simulation to pause immediately before the first step
        Thread(simulation).start() // this will pause the simulation without executing any step
        simulation.play()
        val status = simulation.waitFor(Status.PAUSED, AlchemistMultiVesta.MAX_WAIT_SECONDS, TimeUnit.SECONDS)
        check(status == Status.PAUSED) {
            "Simulation did not pause after ${AlchemistMultiVesta.MAX_WAIT_SECONDS} seconds"
        }
        logger.info("Simulation paused at time ${simulation.time}")
    }
}
