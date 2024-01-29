/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content

import graphql.api.EnvironmentApi
import io.kvision.core.AlignItems
import io.kvision.core.Border
import io.kvision.core.BorderStyle
import io.kvision.core.Color
import io.kvision.core.Cursor
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.core.onEvent
import io.kvision.core.onInput
import io.kvision.form.number.range
import io.kvision.html.canvas
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.progress.progress
import io.kvision.redux.createTypedReduxStore
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.utils.px
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasRenderingContext2D
import stores.EnvironmentStore
import stores.actions.EnvironmentStateAction
import stores.actions.ScaleTranslateAction
import stores.reducers.scaleTranslateReducer
import stores.states.ScaleTranslateState
import kotlin.math.PI
import kotlin.math.min
import kotlin.random.Random

class SimulationContext : SimplePanel(className = "simulation-context") {

    companion object {
        const val DEFUALT_NODE_RADIUS = 5.0
        const val DEFAULT_HEIGHT = 1000
        const val DEFAULT_WIDTH = 1000
        const val DEFAULT_SCALE = 25
        const val DEFAULT_START_POSITION = 0
        const val DEFAULT_SCALE_RATIO = 0.8
        const val DEFAULT_NODE_COLOR = "#FF0000"
    }
    private val scaleTranslationStore = createTypedReduxStore(::scaleTranslateReducer, ScaleTranslateState())
    private var nodesRadius: ObservableValue<Double> = ObservableValue(DEFUALT_NODE_RADIUS)

    private fun randomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        repeat(6) { color += letters[Random.nextInt(15)] }
        return color
    }

    private fun CanvasRenderingContext2D.drawNode(position: Pair<Double, Double>, color: String = DEFAULT_NODE_COLOR) {
        beginPath()
        arc(
            position.first,
            position.second,
            nodesRadius.value * 1 / scaleTranslationStore.getState().scale,
            0.0,
            2 * PI,
            false,
        )
        fillStyle = color
        fill()
        closePath()
    }

    private fun CanvasRenderingContext2D.redrawNodes(
        scale: Double = DEFAULT_SCALE.toDouble(),
        translation: Pair<Double, Double> =
            Pair(DEFAULT_START_POSITION.toDouble(), DEFAULT_START_POSITION.toDouble()),
    ) {
        println("Redrawing function")
        val translationScaled = Pair<Double, Double>(
            translation.first * 1 / scaleTranslationStore.getState().scale,
            translation.second * 1 / scaleTranslationStore.getState().scale,
        )

        scale(scale, scale)
        translate(translationScaled.first, translationScaled.second)

        clearRect(
            -translationScaled.first,
            -translationScaled.second,
            DEFAULT_WIDTH.toDouble(),
            DEFAULT_HEIGHT.toDouble(),
        )

        // println("Translated to:"+translationScaled.first+", "+translationScaled.second)

        EnvironmentStore.store.getState().nodes.forEach {
            drawNode(Pair(it.position.coordinates[0], it.position.coordinates[1]), randomColor())
        }

        setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
    }

    init {
        /*CoroutineScope(Dispatchers.CIO).launch {
            println("Init scope update environment store")
            EnvironmentApi.environMentSubScription().collect { response ->
                //async{
                println("data-->"+response.data)
                //}.await()

                //EnvironmentStore.store.dispatch(EnvironmentStateAction.SetNodes(it.data?.environment?.nodeToPos!!.entries.toMutableList()))
            }
        }*/

        CoroutineScope(Dispatchers.Default).launch {
            EnvironmentApi.environMentSubScription().collect {
                // println("A")
                EnvironmentStore.store.dispatch(EnvironmentStateAction.AddAllNodes(it.data?.environment?.nodeToPos!!.entries))
            }
        }

        hPanel(
            FlexWrap.NOWRAP,
            JustifyContent.START,
            AlignItems.START,
            spacing = 5,
        ) {
            canvas(className = "environment-renderer") {
                canvasWidth = DEFAULT_WIDTH
                canvasHeight = DEFAULT_HEIGHT
                border = Border(width = 1.px, style = BorderStyle.SOLID, color = Color("#ff0000"))

                var cor: Job

                var mouseIsDown = false

                var translatePos: Pair<Double, Double> =
                    Pair(DEFAULT_START_POSITION.toDouble(), DEFAULT_START_POSITION.toDouble())

                var startDragOffset: Pair<Double, Double> =
                    Pair(DEFAULT_START_POSITION.toDouble(), DEFAULT_START_POSITION.toDouble())

                addBeforeDisposeHook {
                }

                addAfterCreateHook {

                    this.bind(scaleTranslationStore) { state ->
                        context2D.redrawNodes(state.scale, state.translate)
                    }

                    this.bind(nodesRadius) {
                        context2D.redrawNodes(
                            scaleTranslationStore.getState().scale,
                            scaleTranslationStore.getState().translate,
                        )
                    }

                    this.bind(EnvironmentStore.store) {
                        println("Bind to store")
                        context2D.redrawNodes(
                            scaleTranslationStore.getState().scale,
                            scaleTranslationStore.getState().translate,
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
                        if (e.deltaY > 0) {
                            scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetScale(
                                    scaleTranslationStore.getState().scale * DEFAULT_SCALE_RATIO,
                                ),
                            )
                        } else {
                            scaleTranslationStore.dispatch(
                                ScaleTranslateAction.SetScale(
                                    scaleTranslationStore.getState().scale / DEFAULT_SCALE_RATIO,
                                ),
                            )
                        }
                    }
                }
            }

            vPanel {
                div().bind(scaleTranslationStore) { state -> +"Scale: ${state.scale}" }

                div().bind(scaleTranslationStore) { state ->
                    +"Translation: ${state.translate.first}, ${state.translate.second}"
                }

                range {
                    label = "Node radius $min - $max"
                    min = DEFUALT_NODE_RADIUS
                    max = DEFUALT_NODE_RADIUS * 20
                    step = 1.0
                    value = DEFUALT_NODE_RADIUS
                    onInput {
                        nodesRadius.value = getValue()!!.toDouble()
                    }
                }

                progress(max = scaleTranslationStore.store.getState().scale) {
                    // this.bounds = (ObservableValue(Bounds(min = 0,)))
                }.bind(scaleTranslationStore) {
                }

                div().bind(nodesRadius) {
                    +"Node radius: ${nodesRadius.value}"
                }
            }
        }
    }
}
