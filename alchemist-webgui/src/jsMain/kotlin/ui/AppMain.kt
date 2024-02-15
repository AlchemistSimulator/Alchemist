/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package ui

import components.Appbar
import components.Content
import io.kvision.Application
import io.kvision.panel.root

/**
 * Represents the main entry point of the application.
 * Extends the Application class to define the application lifecycle.
 */
class AppMain : Application() {

    /**
     * Override of the start method to initialize and configure the application.
     * Sets up the root UI element and adds the Appbar and Content components to it.
     */
    override fun start() {
        root(id = "root") {
            add(Appbar(className = "appbar-root"))
            add(Content(className = "content-root"))
        }
    }
}
