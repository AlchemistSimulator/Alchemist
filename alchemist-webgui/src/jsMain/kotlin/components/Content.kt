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
import io.kvision.core.Background
import io.kvision.core.BoxShadow
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.CssSize
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.core.UNIT
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.utils.perc
import io.kvision.utils.px

open class Content(className: String = "") : SimplePanel(className = className) {

    init {

        // val offcanvas = offcanvas("Lorem ipsum", OffPlacement.END, dark = true)
        // offcanvas.show()

        /*flexPanel(
            FlexDirection.ROW,
            FlexWrap.WRAP,
            JustifyContent.CENTER,
            AlignItems.START,
            spacing = 5,
        ) {*/
            /*div(className = "nodes-list") {

                bind(EnvironmentStore.store) { env ->
                    env.nodes.forEach {
                        div { +"position: ${it.position.coordinates}" }
                    }
                }
            }*/

            hPanel(
                FlexWrap.NOWRAP,
                JustifyContent.CENTER,
                AlignItems.START,
                spacing = 5,
            ) {
                background = Background(color = Color.name(Col.LIGHTGRAY))

                add(
                    SimulationContext(className = "simulation-context").apply{
                        width = 1400.px
                        height = 900.px
                    }
                )
                add(
                    vPanel(
                        JustifyContent.CENTER,
                        AlignItems.START,
                        spacing = 5,
                    ) {
                        width = 520.px
                        add(
                            SimulationIndicators(className = "simulation-indicators").apply{
                                width = 520.px
                            }
                        )
                        div{
                            flexGrow = 1
                            height = 100.perc
                            borderRadius = CssSize(10, UNIT.px)
                            boxShadow = BoxShadow(0.px, 0.px, 5.px, 0.px, Color.rgba(0, 0, 0, (0.5 * 255).toInt()))
                            background = Background(color = Color.name(Col.WHITE))
                        }

                    }
                )
            }
        //}
    }
}
