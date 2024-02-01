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
import io.kvision.core.AlignContent
import io.kvision.core.AlignItems
import io.kvision.core.Background
import io.kvision.core.Border
import io.kvision.core.BorderStyle
import io.kvision.core.BoxShadow
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.CssSize
import io.kvision.core.Cursor
import io.kvision.core.FlexDirection
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.core.UNIT
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
import stores.actions.EnvironmentStateAction
import stores.actions.ScaleTranslateAction
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.random.Random

class SimulationContext(className: String = "") : SimplePanel(className = className) {


    data class Node(val id: String, val x: Double, val y: Double, val color: String)

    private val nodes = List(50) { index ->
        Node("${index + 1}", Random.nextDouble(0.0, 400.0), Random.nextDouble(0.0, 200.0), randomColor())
    }

    private fun randomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        repeat(6) { color += letters[Random.nextInt(15)] }
        return color
    }

    private fun CanvasRenderingContext2D.drawNode(
        position: Pair<Double, Double>,
        color: String = CommonProperties.RenderProperties.DEFAULT_NODE_COLOR,
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
        scale: Double = CommonProperties.Observables.scaleTranslationStore.getState().scale,
        translation: Pair<Double, Double> = CommonProperties.Observables.scaleTranslationStore.getState().translate,
    ) {
        println("Function[Redraw]: Redrawing function")
        val translationScaled = Pair(
            translation.first * 1 / scale,
            translation.second * 1 / scale,
        )

        scale(scale, scale)
        translate(translationScaled.first, translationScaled.second)

        clearRect(
            -translationScaled.first,
            -translationScaled.second,
            CommonProperties.RenderProperties.DEFAULT_WIDTH,
            CommonProperties.RenderProperties.DEFAULT_HEIGHT,
        )

        drawGrid()

        /*EnvironmentStore.store.getState().nodes.forEach {
            drawNode(Pair(it.position.coordinates[0], it.position.coordinates[1]), randomColor())
        }*/

        nodes.forEach {
            drawNode(Pair(it.x, it.y), it.color)
        }

        setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
    }

    private fun CanvasRenderingContext2D.drawGrid(){
        strokeStyle = "#ccc"
        lineWidth = 1.0 / CommonProperties.Observables.scaleTranslationStore.getState().scale

        val stepP = 10
        val left = 0.5 - ceil(CommonProperties.RenderProperties.DEFAULT_WIDTH / stepP) * stepP
        val top = 0.5 - ceil(CommonProperties.RenderProperties.DEFAULT_HEIGHT / stepP) * stepP
        val right = 2 * CommonProperties.RenderProperties.DEFAULT_WIDTH
        val bottom = 2 * CommonProperties.RenderProperties.DEFAULT_HEIGHT
        clearRect(left, top, right - left, bottom - top)
        beginPath()
        for (x in left.toInt() until right.toInt() + stepP step stepP) {
            moveTo(x.toDouble(), top)
            lineTo(x.toDouble(), bottom)
        }
        for (y in top.toInt() until bottom.toInt() + stepP step stepP) {
            moveTo(left, y.toDouble())
            lineTo(right, y.toDouble())
        }
        strokeStyle = "#ADADAD"
        stroke()
    }

    init {

        border = Border(width = 1.px, BorderStyle.SOLID, Color("#ff0000"))

        CoroutineScope(Dispatchers.Default).launch {
            println("COROUTINE[environmentSubscription]: Environmnent subscription started")
            EnvironmentApi.environMentSubScription().collect {
                // println("A")
                EnvironmentStore.store.dispatch(EnvironmentStateAction.AddAllNodes(it.data?.environment?.nodeToPos!!.entries))
            }
            println("COROUTINE[environmentSubscription]: Environmnent subscription ended")
        }

        flexPanel(
            FlexDirection.COLUMN,
            FlexWrap.NOWRAP,
            JustifyContent.CENTER,
            AlignItems.CENTER,
            AlignContent.CENTER
        ) {
            height = 100.perc

           canvas(className = "environment-renderer") {
                canvasWidth = CommonProperties.RenderProperties.DEFAULT_WIDTH.toInt()
                canvasHeight = CommonProperties.RenderProperties.DEFAULT_HEIGHT.toInt()
                borderRadius = CssSize(10, UNIT.px)
                boxShadow = BoxShadow(0.px, 0.px, 5.px, 0.px, Color.rgba(0, 0, 0, (0.5 * 255).toInt()))
                background = Background(color = Color.name(Col.WHITE))

                var mouseIsDown = false

                var translatePos: Pair<Double, Double>

                var startDragOffset: Pair<Double, Double> =
                    Pair(
                        CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble(),
                        CommonProperties.RenderProperties.DEFAULT_START_POSITION.toDouble(),
                    )
                lateinit var boundingRect: DOMRect


                addAfterCreateHook {

                    boundingRect = getElement()!!.getBoundingClientRect()

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

                onEvent {
                    mousedown = { e ->
                        cursor = Cursor.GRABBING
                        mouseIsDown = true
                        startDragOffset = Pair(
                            e.clientX - CommonProperties.Observables.scaleTranslationStore.getState().translate.first,
                            e.clientY - CommonProperties.Observables.scaleTranslationStore.getState().translate.second
                        )
                    }

                    mousemove = { e ->
                        if (mouseIsDown) {

                            translatePos = Pair(
                                e.clientX - startDragOffset.first,
                                e.clientY - startDragOffset.second
                            )

                            CommonProperties.Observables.scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetTranslation(translatePos)
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
                            val translationChangeX = (1 - scaleChangeFactor) * (e.clientX - boundingRect.left - currentState.translate.first)
                            val translationChangeY = (1 - scaleChangeFactor) * (e.clientY - boundingRect.top - currentState.translate.second)

                            translatePos = Pair(
                                translationChangeX,
                                translationChangeY
                            )

                            CommonProperties.Observables.scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetScale(nextScale),

                            )
                            CommonProperties.Observables.scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetTranslation(
                                    Pair(
                                        currentState.translate.first + translationChangeX,
                                        currentState.translate.second + translationChangeY
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
