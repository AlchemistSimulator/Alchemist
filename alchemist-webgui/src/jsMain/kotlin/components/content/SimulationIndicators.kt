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
import io.kvision.core.AlignItems
import io.kvision.core.Background
import io.kvision.core.BoxShadow
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.core.onInput
import io.kvision.form.number.range
import io.kvision.html.div
import io.kvision.html.p
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.panel.vPanel
import io.kvision.progress.progress
import io.kvision.progress.progressNumeric
import io.kvision.state.bind
import io.kvision.utils.perc
import io.kvision.utils.px
import korlibs.math.roundDecimalPlaces

class SimulationIndicators(className: String = "") : SimplePanel(className = className) {

    init {
        boxShadow = BoxShadow(0.px, 0.px, 5.px, 0.px, Color.rgba(0, 0, 0, (0.4 * 255).toInt()))
        background = Background(Color.name(Col.WHITE))
        borderRadius = 10.px
        flexPanel(
            FlexDirection.ROW,
            FlexWrap.NOWRAP,
            JustifyContent.CENTER,
            AlignItems.CENTER,
        ) {
            vPanel {
                justifyContent = JustifyContent.CENTER
                spacing = 5
                width = 95.perc

                div().bind(CommonProperties.Observables.scaleTranslationStore) { state ->
                    +"Scale: ${state.scale.roundDecimalPlaces(2)}"
                }

                div().bind(CommonProperties.Observables.scaleTranslationStore) { state ->

                    div {
                        +"X translation: ${state.translate.first.roundDecimalPlaces(2)}"
                    }

                    div {
                        +"Y translation: ${state.translate.second.roundDecimalPlaces(2)}"
                    }
                }

                range {
                    label = "Node radius: $min ($min - $max)"
                    min = CommonProperties.RenderProperties.DEFUALT_NODE_RADIUS
                    max = CommonProperties.RenderProperties.DEFUALT_NODE_RADIUS * 10
                    step = 1.0
                    value = CommonProperties.RenderProperties.DEFUALT_NODE_RADIUS
                    onInput {
                        CommonProperties.Observables.nodesRadius.value = getValue()!!.toDouble()
                    }
                }.bind(CommonProperties.Observables.nodesRadius) {
                    label = "Node radius: ${CommonProperties.Observables.nodesRadius.value} ($min - $max)"
                }

                /*div().bind(CommonProperties.Observables.nodesRadius) {
                    +"Node radius: ${CommonProperties.Observables.nodesRadius.value}"
                }*/

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
}
