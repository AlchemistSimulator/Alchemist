/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components

import components.content.ContextIndicators
import components.content.NodeProperties
import components.content.SimulationContext
import io.kvision.core.AlignItems
import io.kvision.core.Background
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.utils.perc
import io.kvision.utils.px

/**
 * Class representing the content panel of the application.
 * This class extends the SimplePanel class and provides functionality to display simulation context,
 * simulation indicators, and node properties within a structured layout.
 *
 * @param className the CSS class name to be applied to the content panel
 */
class Content(className: String = "") : SimplePanel(className = className) {

    init {

        hPanel(
            FlexWrap.NOWRAP,
            JustifyContent.CENTER,
            AlignItems.FLEXSTART,
            spacing = 5,

        ) {
            background = Background(color = Color.name(Col.LIGHTGRAY))

            add(
                SimulationContext(className = "simulation-context").apply {
                    width = 1400.px
                    height = 900.px
                },
            )

            vPanel(
                JustifyContent.START,
                AlignItems.CENTER,
                spacing = 5,
            ) {
                width = 520.px
                height = 95.perc
                marginTop = 12.px
                add(
                    ContextIndicators(className = "context-indicators").apply {
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
