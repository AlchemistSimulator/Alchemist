/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.live

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.composeui.ComposeMonitor
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.io.File

/**
 * Opens the desktop UI on the scenario file `args[0]`, adding a [ComposeMonitor] if the scenario has none.
 * Closing the window ends the simulation.
 */
fun main(args: Array<String>) {
    val scenario = File(args.single())
    require(scenario.isFile) { "No scenario at ${scenario.absolutePath}" }
    // The position type only matters to the monitor, which works with any: Euclidean2DPosition just fixes it.
    val simulation: Simulation<Any, Euclidean2DPosition> = LoadAlchemist.from(scenario).getDefault()
    if (simulation.outputMonitors.none { it is ComposeMonitor<*, *> }) {
        simulation.addOutputMonitor(ComposeMonitor())
    }
    simulation.run()
}
