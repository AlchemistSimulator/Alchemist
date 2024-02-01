/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content

import components.content.shared.CommonProperties
import io.kvision.core.Background
import io.kvision.core.Border
import io.kvision.core.BorderStyle
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.onInput
import io.kvision.form.number.range
import io.kvision.html.div
import io.kvision.html.p
import io.kvision.panel.SimplePanel
import io.kvision.panel.vPanel
import io.kvision.progress.progress
import io.kvision.progress.progressNumeric
import io.kvision.state.bind
import io.kvision.utils.perc
import io.kvision.utils.px
import korlibs.math.roundDecimalPlaces

class SimulationIndicators(className: String = "") : SimplePanel(className = className) {

    init {
        border = Border(width = 1.px, BorderStyle.SOLID, Color("#ff0000"))
        height = 100.perc

        vPanel {
            height = 95.perc
            width = 95.perc
            spacing = 5
            borderRadius = 10.px
            background = Background(color = Color.name(Col.WHITE))
            border = Border(width = 2.px, BorderStyle.SOLID, Color("#A3A3A3"))

            div().bind(CommonProperties.Observables.scaleTranslationStore) { state ->
                +"Scale: ${state.scale}"
            }

            div().bind(CommonProperties.Observables.scaleTranslationStore) { state ->
                +"Translation: ${state.translate.first}, ${state.translate.second}"
            }

            range {
                label = "Node radius $min - $max"
                min = CommonProperties.RenderProperties.DEFUALT_NODE_RADIUS
                max = CommonProperties.RenderProperties.DEFUALT_NODE_RADIUS * 20
                step = 1.0
                value = CommonProperties.RenderProperties.DEFUALT_NODE_RADIUS
                onInput {
                    CommonProperties.Observables.nodesRadius.value = getValue()!!.toDouble()
                }
            }

            div().bind(CommonProperties.Observables.nodesRadius) {
                +"Node radius: ${CommonProperties.Observables.nodesRadius.value}"
            }

            p(className = "scale-value").bind(CommonProperties.Observables.scaleTranslationStore) { state ->
                +"Zoom: ${((state.scale/CommonProperties.RenderProperties.MAX_SCALE) * 100).roundDecimalPlaces(0)}%"
            }

            progress(
                min = CommonProperties.RenderProperties.MIN_SCALE,
                max = CommonProperties.RenderProperties.MAX_SCALE,
            ) {
                progressNumeric {
                    striped = false
                }
            }.getFirstProgressBar()!!.bind(CommonProperties.Observables.scaleTranslationStore) { state ->
                value = state.scale
            }
        }
    }
}
