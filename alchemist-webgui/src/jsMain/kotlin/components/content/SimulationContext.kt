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
import io.kvision.core.CssSize
import io.kvision.core.Cursor
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.core.UNIT
import io.kvision.core.onClick
import io.kvision.core.onEvent
import io.kvision.html.canvas
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.state.bind
import io.kvision.utils.perc
import io.kvision.utils.px
import org.w3c.dom.DOMRect
import stores.EnvironmentStore
import stores.actions.ScaleTranslateAction

class SimulationContext(className: String = "") : SimplePanel(className = className) {

    private var translatePos: Pair<Double, Double> = Pair(0.0, 0.0)

    private var startDragOffset: Pair<Double, Double> =
        Pair(
            CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble(),
            CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble(),
        )

    init {
        // border = Border(width = 1.px, BorderStyle.SOLID, Color("#ff0000"))

        EnvironmentStore.callEnvironmentSubscription()

        flexPanel(
            FlexDirection.COLUMN,
            FlexWrap.NOWRAP,
            JustifyContent.CENTER,
            AlignItems.CENTER,
        ) {
            height = 100.perc

            canvas(className = "environment-renderer") {

                lateinit var boundingRect: DOMRect
                var mouseIsDown = false

                canvasWidth = CommonProperties.RenderProperties.DEFAULT_WIDTH.toInt()
                canvasHeight = CommonProperties.RenderProperties.DEFAULT_HEIGHT.toInt()
                borderRadius = CssSize(10, UNIT.px)
                boxShadow = BoxShadow(0.px, 0.px, 5.px, 0.px, Color.rgba(0, 0, 0, (0.4 * 255).toInt()))
                background = Background(color = Color.name(Col.WHITE))

                addAfterCreateHook {

                    bind(CommonProperties.Observables.scaleTranslationStore) { state ->
                        context2D.redrawNodes(state.scale, state.translate)
                    }

                    bind(CommonProperties.Observables.nodesRadius) {
                        context2D.redrawNodes()
                    }

                    bind(EnvironmentStore.store) {
                        println("Bind[EnvironmentStore.store]: Redrawing nodes on store bind")
                        context2D.redrawNodes()
                    }
                }

                addAfterInsertHook {
                    boundingRect = getElement()!!.getBoundingClientRect()
                }

                onClick {
                    findNode(it, boundingRect)
                }

                onEvent {

                    mousedown = { e ->
                        cursor = Cursor.POINTER
                        mouseIsDown = true
                        startDragOffset = Pair(
                            e.clientX - CommonProperties.Observables.scaleTranslationStore.getState().translate.first,
                            e.clientY - CommonProperties.Observables.scaleTranslationStore.getState().translate.second,
                        )
                    }

                    mousemove = { e ->
                        if (mouseIsDown) {
                            cursor = Cursor.GRABBING
                            translatePos = Pair(e.clientX - startDragOffset.first, e.clientY - startDragOffset.second)

                            CommonProperties.Observables.scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetTranslation(translatePos),
                            )
                        }
                    }

                    mouseup = {
                        mouseIsDown = false
                        cursor = Cursor.DEFAULT
                    }

                    mousewheel = { e ->
                        e.preventDefault()
                        val nextScale = CommonProperties.Utils.nextScale(e.deltaY)

                        if (nextScale <= CommonProperties.RenderProperties.MAX_SCALE &&
                            nextScale >= CommonProperties.RenderProperties.MIN_SCALE
                        ) {
                            val currentState = CommonProperties.Observables.scaleTranslationStore.getState()
                            val scaleChangeFactor = nextScale / currentState.scale

                            val translationChangeX =
                                (1 - scaleChangeFactor) * (e.clientX - boundingRect.left - currentState.translate.first)

                            val translationChangeY =
                                (1 - scaleChangeFactor) * (e.clientY - boundingRect.top - currentState.translate.second)

                            translatePos = Pair(translationChangeX, translationChangeY)

                            CommonProperties.Observables.scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetScale(nextScale),

                            )
                            CommonProperties.Observables.scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetTranslation(
                                    Pair(
                                        currentState.translate.first + translationChangeX,
                                        currentState.translate.second + translationChangeY,
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
