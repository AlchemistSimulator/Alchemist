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
import org.apache.ignite.startup.cmdline.CommandLineStartup

/**
 * Launches a service waiting for simulations to be sent over the network.
 */
class IgniteServerLauncher(
    private val serverConfigPath: String?,
) : SimulationLauncher() {

    override fun launch(loader: Loader) = CommandLineStartup.main(arrayOf(serverConfigPath))
}
