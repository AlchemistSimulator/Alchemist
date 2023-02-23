/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("DEPRECATION")

package it.unibo.alchemist.multivesta.adapter.launch

import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.launch.SimulationLauncher
import it.unibo.alchemist.launch.Validation.OK
import it.unibo.alchemist.loader.Loader

/**
 * Launches a single simulation run that can be controlled by MultiVesta.
 */
class AlchemistMultiVestaSimulationLauncher : SimulationLauncher() {
    override val name = "Alchemist + MultiVesta simulation"
    /**
     * The simulation that was launched.
     */
    lateinit var simulation: Simulation<Any, *>
        private set

    override fun additionalValidation(currentOptions: AlchemistExecutionOptions) = with(currentOptions) {
        when {
            headless -> incompatibleWith("headless mode")
            batch -> incompatibleWith("batch mode")
            variables.isNotEmpty() -> incompatibleWith("variable exploration mode")
            distributed != null -> incompatibleWith("distributed execution")
            else -> OK()
        }
    }

    override fun launch(loader: Loader, parameters: AlchemistExecutionOptions) {
        simulation = prepareSimulation(loader, parameters, emptyMap<String, Any>())
        println("Simulation prepared")
        simulation.goToStep(0) // configure the simulation to pause immediately before the first step
        Thread(simulation).start() // this will pause the simulation without executing any step
        println("Simulation paused at step 0")
    }
}
