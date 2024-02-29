/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content

import components.content.common.collapsible
import components.content.shared.CommonProperties
import io.kvision.core.AlignContent
import io.kvision.core.AlignItems
import io.kvision.core.Background
import io.kvision.core.BoxShadow
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.CssSize
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.core.OverflowWrap
import io.kvision.core.UNIT
import io.kvision.html.div
import io.kvision.html.h5
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.state.bind
import io.kvision.utils.perc
import io.kvision.utils.px
import stores.NodeStore

/**
 * Class representing the properties of a node in the application.
 * This class extends SimplePanel and provides a UI component for displaying node information.
 *
 * @param className the CSS class name for styling the panel (optional)
 */
class NodeProperties(className: String = "") : SimplePanel(className = className) {

    init {
        borderRadius = CssSize(10, UNIT.px)
        boxShadow = BoxShadow(0.px, 0.px, 5.px, 0.px, Color.rgba(0, 0, 0, (0.4 * 255).toInt()))
        background = Background(color = Color.name(Col.WHITE))

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

                h5 {
                    +"Node inspection"
                    width = 100.perc
                    height = 100.perc
                }

                hPanel(
                    FlexWrap.NOWRAP,
                    JustifyContent.SPACEEVENLY,
                    AlignItems.CENTER,
                    spacing = 15,
                    className = "nodeinfo-header",
                ) {
                    div {
                        width = 50.px
                        height = 50.px
                        borderRadius = CssSize(height!!.first.toDouble() * 0.5, UNIT.px)
                        background = Background(Color(CommonProperties.RenderProperties.DEFAULT_NODE_COLOR))
                    }
                    flexPanel(
                        FlexDirection.ROW,
                        FlexWrap.NOWRAP,
                        JustifyContent.CENTER,
                        AlignItems.STRETCH,
                        AlignContent.CENTER,
                        spacing = 5,
                    ) {
                        width = 95.perc
                        div {
                            width = 100.perc
                            overflowWrap = OverflowWrap.ANYWHERE
                        }.bind(NodeStore.nodeStore) {
                            +"id: ${it.node?.environment?.nodeById?.id}"
                        }

                        div {
                            width = 100.perc
                            overflowWrap = OverflowWrap.ANYWHERE
                        }.bind(NodeStore.nodeStore) {
                            +"X: ${it.node?.nodePosition?.coordinates?.get(0)}, Y:${it.node?.nodePosition?.coordinates?.get(1)}"
                        }
                    }
                }

                collapsible("properties-collapsible", "Properties") {
                    bind(NodeStore.nodeStore) { state ->
                        state.node?.environment?.nodeById?.properties?.forEach {
                            div {
                                width = 100.perc
                                overflowWrap = OverflowWrap.ANYWHERE
                                +it
                            }
                        }
                    }
                }

                collapsible("contents-collapsible", "Contents") {
                    bind(NodeStore.nodeStore) { state ->
                        state.node?.environment?.nodeById?.contents?.entries?.forEach {

                            hPanel(
                                FlexWrap.NOWRAP,
                                JustifyContent.CENTER,
                                AlignItems.CENTER,
                                spacing = 5,
                            ) {
                                div {
                                    width = 50.perc
                                    overflowWrap = OverflowWrap.ANYWHERE
                                    +"Name: ${it.molecule.name}"
                                }
                                div {
                                    width = 50.perc
                                    overflowWrap = OverflowWrap.ANYWHERE
                                    +"Concentration: ${it.concentration}"
                                }
                            }
                        }
                    }
                }

                collapsible("reactions-collapsible", "Reactions") {
                    bind(NodeStore.nodeStore) { state ->
                        state.node?.environment?.nodeById?.reactions?.forEach {

                            flexPanel(
                                FlexDirection.ROW,
                                FlexWrap.NOWRAP,
                                JustifyContent.CENTER,
                                AlignItems.STRETCH,
                                AlignContent.CENTER,
                                spacing = 5,
                            ) {
                                div {
                                    width = 50.perc
                                    overflowWrap = OverflowWrap.ANYWHERE
                                    +it.inputContext.name
                                }
                                div {
                                    width = 50.perc
                                    overflowWrap = OverflowWrap.ANYWHERE
                                    +it.outputContext.name
                                }
                                div {
                                    width = 100.perc
                                    overflowWrap = OverflowWrap.ANYWHERE
                                    it.node.id
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
