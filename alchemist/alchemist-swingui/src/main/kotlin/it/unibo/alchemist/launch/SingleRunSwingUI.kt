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
import it.unibo.alchemist.boundary.gui.SingleRunGUI
import it.unibo.alchemist.launch.Validation.Invalid
import it.unibo.alchemist.launch.Validation.OK
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.model.interfaces.GeoPosition
import java.awt.GraphicsEnvironment
import javax.swing.JFrame

/**
 * Launches a Swing GUI meant to be used for a single simulation run.
 */
object SingleRunSwingUI : SimulationLauncher() {
    override val name = "Alchemist graphical simulation"

    override fun additionalValidation(currentOptions: AlchemistExecutionOptions) = with(currentOptions) {
        when {
            headless -> incompatibleWith("headless mode")
            batch -> incompatibleWith("batch mode")
            variables.isNotEmpty() -> incompatibleWith("variable exploration mode")
            distributed != null -> incompatibleWith("distributed execution")
            GraphicsEnvironment.isHeadless() -> Invalid(
                "The JVM graphic environment is marked as headless. Cannot show a graphical interface. "
            )
            else -> OK()
        }
    }

    override fun launch(loader: Loader, parameters: AlchemistExecutionOptions) {
        val simulation = prepareSimulation<Any, GeoPosition>(loader, parameters, emptyMap<String, Any>())
        when {
            parameters.graphics == null -> SingleRunGUI.make(simulation, JFrame.EXIT_ON_CLOSE)
            else -> SingleRunGUI.make(simulation, parameters.graphics, JFrame.EXIT_ON_CLOSE)
        }
    }
}
