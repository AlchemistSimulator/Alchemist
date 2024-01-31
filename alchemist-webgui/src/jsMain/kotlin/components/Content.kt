/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components

import components.content.SimulationContext
import components.content.SimulationIndicators
import io.kvision.core.AlignItems
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.panel.hPanel
import io.kvision.state.bind
import stores.EnvironmentStore

open class Content(className: String = "") : SimplePanel(className = className) {

    init {

        // val offcanvas = offcanvas("Lorem ipsum", OffPlacement.END, dark = true)
        // offcanvas.show()

        flexPanel(
            FlexDirection.ROW,
            FlexWrap.WRAP,
            JustifyContent.CENTER,
            AlignItems.START,
            spacing = 5,
        ) {
            /*div(className = "nodes-list") {

                bind(EnvironmentStore.store) { env ->
                    env.nodes.forEach {
                        div { +"position: ${it.position.coordinates}" }
                    }
                }
            }*/

            hPanel(
                FlexWrap.NOWRAP,
                JustifyContent.START,
                AlignItems.START,
                spacing = 5,
            ) {
                add(SimulationContext(className = "simulation-context"))
                add(SimulationIndicators(className = "simulation-indicators"))
            }
        }
    }
}
