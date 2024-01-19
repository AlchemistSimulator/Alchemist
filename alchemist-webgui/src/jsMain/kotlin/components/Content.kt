/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components

/*import ClientConnection
import io.kvision.core.*
import io.kvision.html.canvas
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.redux.createTypedReduxStore
import io.kvision.state.bind
import io.kvision.utils.px
import it.unibo.alchemist.boundary.graphql.client.EnvironmentSubscription
import org.w3c.dom.CanvasRenderingContext2D
import stores.EnvironmentState
import stores.ScaleTranslateState
import stores.actions.ScaleTranslateAction
import stores.reducers.environmentReducer
import stores.reducers.scaleTranslateReducer
import kotlin.math.PI
import kotlin.random.Random

open class Content() : SimplePanel(className = "content-root") {

    companion object {
        const val DEFUALT_NODE_RADIUS = 0.1
        const val DEFAULT_HEIGHT = 1000
        const val DEFAULT_WIDTH = 1000
        const val DEFAULT_SCALE = 5
        const val DEFAULT_START_POSITION = 500
        const val DEFAULT_SCALE_RATIO = 0.8
    }

    val connection = ClientConnection();

    private val store = createTypedReduxStore(::environmentReducer, EnvironmentState())

//    private fun performGraphQLQuery(store: TypedReduxStore<EnvironmentState, EnvironmentStateAction>) {
//        MainScope().launch {
//            // Simulate a delayed query
//            val result = connection.environMentSubScription().await()
//
//            // Dispatch the result to update the Redux state
//            store.dispatch(EnvironmentStateAction.SetNodes(result))
//        }
//
//    }

    private fun randomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        repeat(6) {
            color += letters[Random.nextInt(15)]
        }
        return color
    }

    private fun redraw(nodes: List<EnvironmentSubscription.Entry>?, context2D: CanvasRenderingContext2D, scale: Double = DEFAULT_SCALE.toDouble() ,translation: Pair<Double,Double> = Pair(DEFAULT_START_POSITION.toDouble(),
        DEFAULT_START_POSITION.toDouble())){
        //context2D.save()
        context2D.scale(scale,scale)
        context2D.translate(translation.first,translation.second)
        context2D.clearRect(-translation.first,-translation.second, DEFAULT_WIDTH.toDouble(),DEFAULT_HEIGHT.toDouble())
        nodes?.forEach {
            context2D.beginPath()
            context2D.arc(it.position.coordinates[0], it.position.coordinates[1], DEFUALT_NODE_RADIUS, 0.0, 2 * PI, false)
            context2D.fillStyle = randomColor()
            context2D.fill()
            context2D.closePath()
        }
        context2D.setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);
    }

    init {

        //performGraphQLQuery(store);

        flexPanel(
            io.kvision.core.FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.CENTER, AlignItems.START,
            spacing = 5
        ) {


            div(className = "nodes-list").bind(store) { env ->
                env.nodes?.environment?.nodeToPos?.entries?.forEach {
                    div {
                        +"position: ${it.position.coordinates}"
                    }
                }
            }

            div(className = "nodes-representation") {

                val scaleTranslationStore = createTypedReduxStore(::scaleTranslateReducer, ScaleTranslateState())
                div().bind(scaleTranslationStore){ state ->
                    +"Scale: ${state.scale}"
                }

                div().bind(scaleTranslationStore){ state ->
                    +"Translation: ${state.translate.first}, ${state.translate.second}"
                }

                canvas(className = "environment-renderer"){
                    canvasWidth = DEFAULT_WIDTH
                    canvasHeight = DEFAULT_HEIGHT
                    border = Border(width = 1.px, style = BorderStyle.SOLID, color = Color("#ff0000"));

                    var mouseIsDown = false

                    var translatePos: Pair<Double,Double> = Pair(
                        DEFAULT_START_POSITION.toDouble(),
                        DEFAULT_START_POSITION.toDouble())

                    var startDragOffset: Pair<Double,Double> = Pair(
                        DEFAULT_START_POSITION.toDouble(),
                        DEFAULT_START_POSITION.toDouble())

                    var nodes: List<EnvironmentSubscription.Entry>? = emptyList()


                    addAfterInsertHook {

                        this.bind(scaleTranslationStore) { state ->
                            redraw(nodes, context2D, state.scale, state.translate);
                        }

                        this.bind(store){ env ->
                            //context2D = (it.elm as HTMLCanvasElement).getContext("2d") as CanvasRenderingContext2D
                            nodes = env.nodes?.environment?.nodeToPos?.entries
                            redraw(
                                nodes,
                                context2D,
                                scaleTranslationStore.getState().scale,
                                scaleTranslationStore.getState().translate)
                        }

                    }

                    onEvent {
                        mousedown = { e->
                            cursor = Cursor.GRABBING
                            mouseIsDown = true
                            startDragOffset = Pair(e.clientX - translatePos.first, e.clientY - translatePos.second)
                        }
                        mousemove = { e->
                            if(mouseIsDown){
                                translatePos = Pair(e.clientX - startDragOffset.first, e.clientY - startDragOffset.second)
                                scaleTranslationStore.dispatch(ScaleTranslateAction.SetTranslation(translatePos));
                            }
                        }
                        mouseup = {
                            mouseIsDown = false
                            cursor = Cursor.DEFAULT
                        }
                        mousewheel = { e->
                            e.preventDefault()
                            if(e.deltaY > 0) {
                                scaleTranslationStore.dispatch(
                                    ScaleTranslateAction.SetScale(
                                        scaleTranslationStore.getState().scale * DEFAULT_SCALE_RATIO));
                            }else
                                scaleTranslationStore.dispatch(
                                    ScaleTranslateAction.SetScale(
                                        scaleTranslationStore.getState().scale / DEFAULT_SCALE_RATIO));
                        }
                    }
                }
            }
        }
    }
}*/
