/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client

import it.unibo.alchemist.client.components.AppContent
import it.unibo.alchemist.client.components.AppNavbar
import kotlinx.browser.document
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import web.dom.Element

/**
 * The entry point of the Kotlin/JS application. Find the root element and render the App.
 */
fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find container!")
    createRoot(container.unsafeCast<Element>()).render(App.create())
}

/**
 * The App to render.
 */
val App: FC<Props> = FC {
    AppNavbar()
    AppContent()
}
