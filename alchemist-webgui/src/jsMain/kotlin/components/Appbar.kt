/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components

import components.navbar.PlayButton
import io.kvision.core.AlignItems
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.FlexDirection
import io.kvision.html.div
import io.kvision.navbar.NavbarColor
import io.kvision.navbar.NavbarType
import io.kvision.navbar.nav
import io.kvision.navbar.navbar
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.state.bind
import stores.SimulationStatus

/**
 * Class representing the application's top navigation bar.
 *
 * @param className the CSS class name to be applied to the navigation bar
 */
class Appbar(className: String = "") : SimplePanel(className = className) {

    private val simulationFileName = ""

    init {
        navbar(
            if (simulationFileName.isEmpty()) {
                "Alchemist Web Interface"
            } else {
                "Alchemist Web Interface - $simulationFileName"
            },
            collapseOnClick = false,
        ) {
            type = NavbarType.STICKYTOP
            nColor = NavbarColor.DARK

            nav(rightAlign = true) {
                flexPanel {
                    flexDirection = FlexDirection.ROW
                    alignItems = AlignItems.CENTER
                    spacing = 15
                    div {
                        color = Color.name(Col.WHITE)
                    }.bind(SimulationStatus.simulationStore) { sim ->
                        +"STATUS: ${sim.status?.simulationStatus}"
                    }

                    add(PlayButton("Play"))
                }
            }
        }
    }
}
