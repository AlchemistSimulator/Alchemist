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
import it.unibo.alchemist.launch.Validation.Invalid
import it.unibo.alchemist.launch.Validation.OK
import org.apache.ignite.startup.cmdline.CommandLineStartup

/**
 * Launches a service waiting for simulations to be sent over the network.
 */
object IgniteServerLauncher : AbstractLauncher() {

    override val name = "Alchemist grid computing server"

    override fun validate(currentOptions: AlchemistExecutionOptions): Validation = with(currentOptions) {
        when {
            configuration != null -> incompatibleWith("direct execution of a simulation file")
            variables.isNotEmpty() -> incompatibleWith("simulation variables set")
            batch -> incompatibleWith("batch mode")
            distributed != null -> incompatibleWith("distributed execution")
            graphics != null -> incompatibleWith("graphic effects enabled")
            help -> Invalid("There is no specific help for server mode")
            server == null -> Invalid("No Ignite configuration file specified")
            interval != AlchemistExecutionOptions.defaultInterval -> incompatibleWith("custom sampling intervals")
            parallelism != AlchemistExecutionOptions.defaultParallelism -> incompatibleWith("custom parallelism")
            endTime != AlchemistExecutionOptions.defaultEndTime -> incompatibleWith("simulation end time")
            else -> OK()
        }
    }

    override fun launch(parameters: AlchemistExecutionOptions) = CommandLineStartup.main(arrayOf(parameters.server))
}
