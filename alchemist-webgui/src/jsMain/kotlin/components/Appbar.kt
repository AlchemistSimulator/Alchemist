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
import io.kvision.core.Color
import io.kvision.html.div
import io.kvision.navbar.NavbarColor
import io.kvision.navbar.NavbarType
import io.kvision.navbar.nav
import io.kvision.navbar.navbar
import io.kvision.panel.SimplePanel
import io.kvision.state.bind
import stores.SimulationStatus

open class Appbar() : SimplePanel(className = "appbar-root") {

    private val simulationFileName = "simulationTest.yml"

    init {
        // SimulationStatus.callGetStatus()
        navbar(
            if (simulationFileName.isEmpty()) "Alchemist" else "Alchemist - $simulationFileName",
            collapseOnClick = false,
        ) {
            type = NavbarType.STICKYTOP
            nColor = NavbarColor.DARK

            nav {

                div {
                    color = Color("#ffffff")
                }.bind(SimulationStatus.simulationStore) { sim ->
                    val simStatus = sim.status?.simulationStatus
                    +"SIMULATION STATUS IS: $simStatus"
                }
            }

            nav(rightAlign = true) {
                add(PlayButton("Play"))
            }
        }
    }
}
