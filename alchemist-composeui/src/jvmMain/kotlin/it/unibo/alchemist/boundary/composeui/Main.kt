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

/**
 * Main entry point of the application.
 */
fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Alchemist",
        ) {
            app()
        }
    }
