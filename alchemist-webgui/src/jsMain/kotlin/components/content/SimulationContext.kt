/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content

import components.content.shared.CommonProperties.Observables.nodesRadius
import components.content.shared.CommonProperties.Observables.scaleTranslationStore
import components.content.shared.CommonProperties.RenderProperties
import components.content.shared.CommonProperties.Utils.nextScale
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.DOMRect
import stores.EnvironmentStore
import stores.SimulationStatus
import stores.actions.ScaleTranslateAction
import utils.SimState

/**
 * Class representing the simulation context in the application.
 * This class extends SimplePanel and provides a UI component for rendering the simulation environment.
 *
 * @param className the CSS class name for styling the panel (optional)
 */
class SimulationContext(className: String = "") : SimplePanel(className = className) {

    private lateinit var canvasCtxt: CanvasRenderingContext2D

    private var translatePos: Pair<Double, Double> = Pair(0.0, 0.0)

    private var startDragOffset: Pair<Double, Double> =
        Pair(
            RenderProperties.DEFAULT_START_POSITION.toDouble(),
            RenderProperties.DEFAULT_START_POSITION.toDouble(),
        )

    init {

        EnvironmentStore.callEnvironmentQuery()

        // Workaround for node re-render as bind function seems to reload the whole object in the DOM
        EnvironmentStore.store.subscribe { state ->
            if (::canvasCtxt.isInitialized) {
                canvasCtxt.redrawNodes(state.toListOfPairs())
            }
        }

        SimulationStatus.simulationStore.subscribe { sim ->
            if (SimState.toSimStatus(sim.status?.simulationStatus) == SimState.RUNNING) {
                CoroutineScope(Dispatchers.Default).launch {
                    EnvironmentStore.callEnvironmentSubscription()
                }
            }
        }

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

                canvasWidth = RenderProperties.DEFAULT_WIDTH.toInt()
                canvasHeight = RenderProperties.DEFAULT_HEIGHT.toInt()
                borderRadius = CssSize(10, UNIT.px)
                boxShadow = BoxShadow(0.px, 0.px, 5.px, 0.px, Color.rgba(0, 0, 0, (0.4 * 255).toInt()))
                background = Background(color = Color.name(Col.WHITE))

                addAfterInsertHook {
                    boundingRect = getElement()!!.getBoundingClientRect()
                    canvasCtxt = context2D

                    // Sets the canvas to a centered position
                    translatePos = Pair(RenderProperties.DEFAULT_WIDTH / 2, RenderProperties.DEFAULT_HEIGHT / 2)
                    scaleTranslationStore.dispatch(ScaleTranslateAction.SetTranslation(translatePos))

                    context2D.redrawNodes(EnvironmentStore.store.getState().toListOfPairs())

                    bind(scaleTranslationStore) { state ->
                        context2D.redrawNodes(
                            EnvironmentStore.store.getState().toListOfPairs(),
                            state.scale,
                            state.translate,
                        )
                    }

                    bind(nodesRadius) {
                        context2D.redrawNodes(EnvironmentStore.store.getState().toListOfPairs())
                    }
                }

                onClick {
                    findNode(it, boundingRect)
                }

                onEvent {

                    mousedown = { e ->
                        mouseIsDown = true
                        startDragOffset = Pair(
                            e.clientX - scaleTranslationStore.getState().translate.first,
                            e.clientY - scaleTranslationStore.getState().translate.second,
                        )
                    }

                    mousemove = { e ->
                        if (mouseIsDown) {
                            cursor = Cursor.GRABBING
                            translatePos = Pair(e.clientX - startDragOffset.first, e.clientY - startDragOffset.second)

                            scaleTranslationStore.dispatch(
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
                        val nextScale = nextScale(e.deltaY)

                        if (nextScale <= RenderProperties.MAX_SCALE &&
                            nextScale >= RenderProperties.MIN_SCALE
                        ) {
                            val currentState = scaleTranslationStore.getState()
                            val scaleChangeFactor = nextScale / currentState.scale

                            val translationChangeX =
                                (1 - scaleChangeFactor) * (e.clientX - boundingRect.left - currentState.translate.first)

                            val translationChangeY =
                                (1 - scaleChangeFactor) * (e.clientY - boundingRect.top - currentState.translate.second)

                            translatePos = Pair(translationChangeX, translationChangeY)

                            scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetScale(nextScale),

                            )
                            scaleTranslationStore.dispatch(
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
