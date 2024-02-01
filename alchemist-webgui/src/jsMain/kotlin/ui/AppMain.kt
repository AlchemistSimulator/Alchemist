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

class AppMain : Application() {
    override fun start() {
        root(id = "root") {
            add(Appbar(className = "appbar-root"))
            add(Content(className = "content-root"))
        }
    }
}
