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
import io.kvision.core.AlignItems
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.state.bind
import stores.EnvironmentStore

open class Content() : SimplePanel(className = "content-root") {

    init {

        // EnvironmentStore.callEnvironmentSubscription()

        // val offcanvas = offcanvas("Lorem ipsum", OffPlacement.END, dark = true)

        flexPanel(
            FlexDirection.ROW,
            FlexWrap.WRAP,
            JustifyContent.CENTER,
            AlignItems.START,
            spacing = 5,
        ) {
            div(className = "nodes-list") {

                bind(EnvironmentStore.store) { env ->
                    env.nodes.forEach {
                        div { +"position: ${it.position.coordinates}" }
                    }
                }
            }

            val simulationContext = SimulationContext()
            add(simulationContext)
        }

        // offcanvas.show()
    }
}
