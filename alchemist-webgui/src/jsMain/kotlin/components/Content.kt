/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components

import components.content.NodeProperties
import components.content.SimulationContext
import components.content.SimulationIndicators
import io.kvision.core.AlignItems
import io.kvision.core.Background
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.panel.hPanel
import io.kvision.utils.perc
import io.kvision.utils.px

open class Content(className: String = "") : SimplePanel(className = className) {

    init {

        // val offcanvas = offcanvas("Lorem ipsum", OffPlacement.END, dark = true)
        // offcanvas.show()

        hPanel(
            FlexWrap.NOWRAP,
            JustifyContent.CENTER,
            AlignItems.CENTER,
            spacing = 5,

        ) {
            background = Background(color = Color.name(Col.LIGHTGRAY))

            add(
                SimulationContext(className = "simulation-context").apply {
                    width = 1400.px
                    height = 900.px
                },
            )

            flexPanel(
                FlexDirection.COLUMN,
                FlexWrap.NOWRAP,
                JustifyContent.START,
                AlignItems.START,
                spacing = 5,
            ) {
                width = 520.px
                height = 95.perc
                add(
                    SimulationIndicators(className = "simulation-indicators").apply {
                        width = 100.perc
                        height = 100.perc
                    },
                )
                add(
                    NodeProperties(className = "node-properties").apply {
                        width = 100.perc
                        height = 100.perc
                    },
                )
            }
        }
    }
}
