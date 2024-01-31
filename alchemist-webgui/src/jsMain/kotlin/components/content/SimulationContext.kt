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
import graphql.api.EnvironmentApi
import io.kvision.core.Border
import io.kvision.core.BorderStyle
import io.kvision.core.BoxShadow
import io.kvision.core.Color
import io.kvision.core.CssSize
import io.kvision.core.Cursor
import io.kvision.core.UNIT
import io.kvision.core.onEvent
import io.kvision.html.canvas
import io.kvision.panel.SimplePanel
import io.kvision.state.bind
import io.kvision.utils.px
import korlibs.image.color.Colors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasRenderingContext2D
import stores.EnvironmentStore
import stores.actions.EnvironmentStateAction
import stores.actions.ScaleTranslateAction
import kotlin.math.PI
import kotlin.random.Random

class SimulationContext(className: String = "") : SimplePanel(className = className) {

    private fun randomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        repeat(6) { color += letters[Random.nextInt(15)] }
        return color
    }

    private fun CanvasRenderingContext2D.drawNode(
        position: Pair<Double, Double>,
        color: String = CommonProperties.RenderProperties.DEFAULT_NODE_COLOR
    ) {
        beginPath()
        arc(
            position.first,
            position.second,
            CommonProperties.Observables.nodesRadius.value * 1 /
                CommonProperties.Observables.scaleTranslationStore.getState().scale,
            0.0,
            2 * PI,
            false,
        )
        fillStyle = color
        fill()
        closePath()
    }

    private fun CanvasRenderingContext2D.redrawNodes(
        scale: Double = CommonProperties.RenderProperties.DEFAULT_SCALE.toDouble(),
        translation: Pair<Double, Double> =
            Pair(CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble(),
                CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble()),
    ) {
        println("Function[Redraw]: Redrawing function")
        val translationScaled = Pair(
            translation.first * 1 / CommonProperties.Observables.scaleTranslationStore.getState().scale,
            translation.second * 1 / CommonProperties.Observables.scaleTranslationStore.getState().scale,
        )

        scale(scale, scale)
        translate(translationScaled.first, translationScaled.second)

        clearRect(
            -translationScaled.first,
            -translationScaled.second,
            CommonProperties.RenderProperties.DEFAULT_WIDTH,
            CommonProperties.RenderProperties.DEFAULT_HEIGHT,
        )

        /*beginPath()
        strokeStyle = "#000000"
        moveTo((CommonProperties.RenderProperties.DEFAULT_WIDTH / 2), 0.0)
        lineTo((CommonProperties.RenderProperties.DEFAULT_WIDTH / 2), CommonProperties.RenderProperties.DEFAULT_HEIGHT)
        moveTo(0.0, (CommonProperties.RenderProperties.DEFAULT_HEIGHT / 2))
        lineTo(CommonProperties.RenderProperties.DEFAULT_WIDTH, (CommonProperties.RenderProperties.DEFAULT_HEIGHT / 2))
        stroke()
        closePath()*/

        EnvironmentStore.store.getState().nodes.forEach {
            drawNode(Pair(it.position.coordinates[0], it.position.coordinates[1]), randomColor())
        }

        setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
    }

    init {

        CoroutineScope(Dispatchers.Default).launch {
            println("COROUTINE[environmentSubscription]: Environmnent subscription started")
            EnvironmentApi.environMentSubScription().collect {
                // println("A")
                EnvironmentStore.store.dispatch(EnvironmentStateAction.AddAllNodes(it.data?.environment?.nodeToPos!!.entries))
            }
            println("COROUTINE[environmentSubscription]: Environmnent subscription ended")
        }

        canvas(className = "environment-renderer") {
            canvasWidth = CommonProperties.RenderProperties.DEFAULT_WIDTH.toInt()
            canvasHeight = CommonProperties.RenderProperties.DEFAULT_HEIGHT.toInt()
            borderRadius = CssSize(10, UNIT.px)
            border = Border(width = 2.px, BorderStyle.SOLID, Color("#A3A3A3"))

            var mouseIsDown = false

            var translatePos: Pair<Double, Double> =
                Pair(CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble(),
                    CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble())

            var startDragOffset: Pair<Double, Double> =
                Pair(CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble(),
                    CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble())

            addAfterCreateHook {

                this.bind(CommonProperties.Observables.scaleTranslationStore) { state ->
                    context2D.redrawNodes(state.scale, state.translate)
                }

                this.bind(CommonProperties.Observables.nodesRadius) {
                    context2D.redrawNodes(
                        CommonProperties.Observables.scaleTranslationStore.getState().scale,
                        CommonProperties.Observables.scaleTranslationStore.getState().translate,
                    )
                }

                this.bind(EnvironmentStore.store) {
                    println("Bind to store")
                    context2D.redrawNodes(
                        CommonProperties.Observables.scaleTranslationStore.getState().scale,
                        CommonProperties.Observables.scaleTranslationStore.getState().translate,
                    )
                }
            }

            addAfterInsertHook {
            }

            onEvent {
                mousedown = { e ->
                    cursor = Cursor.GRABBING
                    mouseIsDown = true
                    startDragOffset =
                        Pair(
                            e.clientX - translatePos.first,
                            e.clientY - translatePos.second,
                        )
                }
                mousemove = { e ->
                    if (mouseIsDown) {
                        translatePos =
                            Pair(
                                e.clientX - startDragOffset.first,
                                e.clientY - startDragOffset.second,
                            )
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

                    if(nextScale <= CommonProperties.RenderProperties.MAX_SCALE &&
                        nextScale >= CommonProperties.RenderProperties.MIN_SCALE) {
                        CommonProperties.Observables.scaleTranslationStore
                            .dispatch(ScaleTranslateAction.SetScale(nextScale))
                    }
                }
            }
        }
    }
}
