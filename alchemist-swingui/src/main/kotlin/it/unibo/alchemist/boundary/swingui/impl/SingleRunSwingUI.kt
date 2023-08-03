/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("DEPRECATION")

package it.unibo.alchemist.boundary.swingui.impl

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.launch.SimulationLauncher
import javax.swing.JFrame

/**
 * Launches a Swing GUI meant to be used for a single simulation run.
 */
class SingleRunSwingUI(
    val graphics: String? = null,
) : SimulationLauncher() {

    override fun launch(loader: Loader) {
        val simulation = prepareSimulation<Any, Nothing>(loader, emptyMap<String, Any>())
        when (graphics) {
            null -> SingleRunGUI.make(simulation, JFrame.EXIT_ON_CLOSE)
            else -> SingleRunGUI.make(simulation, graphics, JFrame.EXIT_ON_CLOSE)
        }
        simulation.run()
    }
}
