/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.launch

import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.boundary.launch.Validation.Invalid
import it.unibo.alchemist.boundary.launch.Validation.OK
import org.apache.ignite.startup.cmdline.CommandLineStartup

/**
 * Launches a service waiting for simulations to be sent over the network.
 */
object IgniteServerLauncher : AbstractLauncher() {

    override val name = "Alchemist grid computing server"

    override fun validate(currentOptions: AlchemistExecutionOptions): Validation = with(currentOptions) {
        when {
            loader != null -> incompatibleWith("direct execution of a simulation file")
            variables.isNotEmpty() -> incompatibleWith("simulation variables set")
            batch -> incompatibleWith("batch mode")
            distributed != null -> incompatibleWith("distributed execution")
            graphics != null -> incompatibleWith("graphic effects enabled")
            help -> Invalid("There is no specific help for server mode")
            server == null -> Invalid("No Ignite configuration file specified")
            parallelism != AlchemistExecutionOptions.defaultParallelism -> incompatibleWith("custom parallelism")
            endTime != AlchemistExecutionOptions.defaultEndTime -> incompatibleWith("simulation end time")
            else -> OK()
        }
    }

    override fun launch(parameters: AlchemistExecutionOptions) = CommandLineStartup.main(arrayOf(parameters.server))
}
