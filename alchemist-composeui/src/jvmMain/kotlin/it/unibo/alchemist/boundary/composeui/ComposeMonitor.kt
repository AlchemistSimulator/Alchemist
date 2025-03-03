/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Environment

/**
 * Monitor extension that uses JVM Compose UI to display the simulation.
 */
class ComposeMonitor : OutputMonitor<Any, Nothing> {
    override fun initialized(environment: Environment<Any, Nothing>) {
        application {
            Window(
                onCloseRequest = { },
                title = "Alchemist",
            ) {
                app()
            }
        }
    }
}
