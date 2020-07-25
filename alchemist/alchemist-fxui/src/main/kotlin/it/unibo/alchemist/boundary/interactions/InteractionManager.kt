/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.interactions

import com.google.common.collect.ImmutableMap
import it.unibo.alchemist.boundary.clear
import it.unibo.alchemist.boundary.createDrawCommand
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor
import it.unibo.alchemist.boundary.makePoint
import it.unibo.alchemist.boundary.monitors.AbstractFXDisplay
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.input.ActionFromKey
import it.unibo.alchemist.input.ActionOnKey
import it.unibo.alchemist.input.ActionOnMouse
import it.unibo.alchemist.input.DynamicMouseEventDispatcher
import it.unibo.alchemist.input.Keybinds
import it.unibo.alchemist.input.KeyboardActionListener
import it.unibo.alchemist.input.KeyboardEventDispatcher
import it.unibo.alchemist.input.MouseButtonTriggerAction
import it.unibo.alchemist.input.NodeBoundMouseEventDispatcher
import it.unibo.alchemist.input.SimpleKeyboardEventDispatcher
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import javafx.event.Event
import javafx.scene.Group
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import java.util.concurrent.Semaphore
import kotlin.math.roundToInt

/**
 * An interaction manager that controls the input/output on the environment done through the GUI.
 * @param monitor the monitor
 */
class InteractionManager<T, P : Position2D<P>>(
    private val monitor: AbstractFXDisplay<T, P>
) {
    /**
     * Describes a certain interaction that has a feedback associated to it.
     */
    private enum class Interaction {
        HIGHLIGHT_CANDIDATE,
        HIGHLIGHTED,
        SELECTION_BOX
    }

    /**
     * The current environment.
     */
    var environment: Environment<T, P>? = null
    /**
     * The nodes in the environment.
     * Should be updated as frequently as possible to ensure a representation of
     * the feedback that is consistent with the actual environment.
     */
    var nodes: Map<Node<T>, P> = emptyMap()
    /**
     * The keyboard listener.
     */
    val keyboardListener: KeyboardActionListener
        get() = keyboard.listener

    private lateinit var wormhole: BidimensionalWormhole<P>
    private lateinit var zoomManager: ZoomManager
    private val keyboard: KeyboardEventDispatcher = SimpleKeyboardEventDispatcher()
    private val keyboardPanManager: DigitalPanManager<P> by lazy {
        DigitalPanManager(wormhole = wormhole) {
            monitor.repaint()
            repaint()
        }
    }
    private val mouse: DynamicMouseEventDispatcher = NodeBoundMouseEventDispatcher(monitor)
    private lateinit var mousePanHelper: AnalogPanHelper
    private val highlighter = Canvas()
    private val selector = Canvas()
    private val selectionHelper: SelectionHelper<T, P> =
        SelectionHelper()
    private val selection: ObservableMap<Node<T>, P> = FXCollections.observableHashMap()
    private val selectionCandidates: ObservableMap<Node<T>, P> = FXCollections.observableHashMap()
    private val selectedElements: ImmutableMap<Node<T>, P>
        get() = ImmutableMap.copyOf(selection)
    private val selectionCandidatesMutex: Semaphore = Semaphore(1)
    @Volatile private var feedback: Map<Interaction, List<() -> Unit>> = emptyMap()
    private val runMutex: Semaphore = Semaphore(1)
    /**
     * The canvases used for input/output.
     */
    val canvases = Group().apply { listOf(highlighter, selector).forEach { children.add(it) } }

    /**
     * Wraps the given command on the simulation to run through [Simulation.schedule]
     * in order to properly run the call on the simulation.
     *
     * For instance, deleting a node:
     * <code>invokeOnSimulation { environment.removeNode(someNode) }</code>
     */
    private val invokeOnSimulation: (Simulation<T, P>.() -> Unit) -> Unit
        get() =
            environment?.simulation?.let { { exec: Simulation<T, P>.() -> Unit -> it.schedule { exec.invoke(it) } } }
                ?: throw IllegalStateException("Uninitialized environment or simulation")

    init {
        listOf(selector, highlighter).forEach {
            it.widthProperty().bind(monitor.widthProperty())
            it.heightProperty().bind(monitor.heightProperty())
            it.isMouseTransparent = true
        }
        highlighter.graphicsContext2D.globalAlpha =
            Alphas.highlight
        selector.graphicsContext2D.globalAlpha =
            Alphas.selection
        // delete
        val deleteNodes = { _: KeyEvent ->
            runMutex.acquireUninterruptibly()
            if (selection.isNotEmpty()) {
                val nodesToRemove: Set<Node<T>> = selectedElements.keys
                selection.clear()
                invokeOnSimulation {
                    nodesToRemove.forEach { environment?.removeNode(it) }
                }
                repaint()
            }
            runMutex.release()
        }
        Keybinds[ActionFromKey.DELETE].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = deleteNodes
        }
        // keyboard-pan
        Keybinds[ActionFromKey.PAN_NORTH].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPanManager += Direction2D.NORTH }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPanManager -= Direction2D.NORTH }
        }
        Keybinds[ActionFromKey.PAN_SOUTH].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPanManager += Direction2D.SOUTH }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPanManager -= Direction2D.SOUTH }
        }
        Keybinds[ActionFromKey.PAN_EAST].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPanManager += Direction2D.EAST }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPanManager -= Direction2D.EAST }
        }
        Keybinds[ActionFromKey.PAN_WEST].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPanManager += Direction2D.WEST }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPanManager -= Direction2D.WEST }
        }
        // move
        val enqueueMove = { _: Event ->
            mouse.setOnActionTemporary(MouseButtonTriggerAction(ActionOnMouse.CLICKED, MouseButton.PRIMARY)) { mouse ->
                runMutex.acquireUninterruptibly()
                if (selection.isNotEmpty()) {
                    val nodesToMove: Map<Node<T>, P> = selectedElements
                    selection.clear()
                    val mousePosition = wormhole.getEnvPoint(makePoint(mouse.x, mouse.y))
                    invokeOnSimulation {
                        nodesToMove.values.maxWith(
                            Comparator { a, b ->
                                (b - a.coordinates).let { it.x + it.y }.roundToInt()
                            }
                        )?.let {
                            mousePosition - it.coordinates
                        }?.let { offset ->
                            environment?.let { env ->
                                nodesToMove.forEach {
                                    env.moveNodeToPosition(it.key, offset)
                                }
                            }
                        }
                    }
                }
                runMutex.release()
            }
        }
        Keybinds[ActionFromKey.MOVE].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = enqueueMove
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.CLICKED, MouseButton.MIDDLE)] = enqueueMove
        // forward one step
        Keybinds[ActionFromKey.ONE_STEP].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = {
                invokeOnSimulation {
                    if (status == Status.PAUSED) {
                        goToStep(step + 1)
                    }
                }
            }
        }
        // select
        mouse[MouseButtonTriggerAction(ActionOnMouse.PRESSED, MouseButton.SECONDARY)] = ::onSelectInitiated
        mouse[MouseButtonTriggerAction(ActionOnMouse.DRAGGED, MouseButton.SECONDARY)] = ::onSelecting
        mouse[MouseButtonTriggerAction(ActionOnMouse.RELEASED, MouseButton.SECONDARY)] = ::onSelected
        mouse[MouseButtonTriggerAction(ActionOnMouse.EXITED, MouseButton.SECONDARY)] = ::onSelectCanceled
        // primary mouse button
        mouse[MouseButtonTriggerAction(ActionOnMouse.PRESSED, MouseButton.PRIMARY)] = {
            when (monitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanInitiated(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectInitiated(it)
                else -> { }
            }
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.DRAGGED, MouseButton.PRIMARY)] = {
            when (monitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanning(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelecting(it)
                else -> { }
            }
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.RELEASED, MouseButton.PRIMARY)] = {
            when (monitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanned(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelected(it)
                else -> { }
            }
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.EXITED, MouseButton.PRIMARY)] = {
            when (monitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanCanceled(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectCanceled(it)
                else -> { }
            }
        }
        // scroll
        monitor.setOnScroll {
            if (it.deltaY != 0.0) {
                zoomManager.inc(it.deltaY / ZOOM_SCALE)
                wormhole.zoomOnPoint(makePoint(it.x, it.y), zoomManager.zoom)
                monitor.repaint()
                it.consume()
            }
        }
        selection.addListener(
            MapChangeListener {
                selection.map {
                    paintHighlight(
                        it.value,
                        Colors.alreadySelected
                    )
                }.let { highlighters ->
                    feedback = feedback + (Interaction.HIGHLIGHTED to highlighters)
                }
                repaint()
            }
        )
        selectionCandidates.addListener(
            MapChangeListener {
                selectionCandidates.map {
                    paintHighlight(
                        it.value,
                        Colors.selecting
                    )
                }.let { highlighters ->
                    feedback = feedback + (Interaction.HIGHLIGHT_CANDIDATE to highlighters)
                }
                repaint()
            }
        )
    }

    /**
     * Called when a pan gesture is initiated.
     * @param event the caller
     */
    private fun onPanInitiated(event: MouseEvent) {
        mousePanHelper = AnalogPanHelper(makePoint(event.x, event.y))
    }

    /**
     * Called while a pan gesture is in progress and the view is moving.
     * @param event the caller
     */
    private fun onPanning(event: MouseEvent) {
        if (mousePanHelper.valid) {
            wormhole.viewPosition = mousePanHelper.update(makePoint(event.x, event.y), wormhole.viewPosition)
            monitor.repaint()
        }
        event.consume()
    }

    /**
     * Called when a pan gesture finishes.
     */
    private fun onPanned(event: MouseEvent) {
        mousePanHelper.close()
        event.consume()
    }

    /**
     * Called when a pan gesture is canceled.
     */
    private fun onPanCanceled(event: MouseEvent) {
        mousePanHelper.close()
        event.consume()
    }

    /**
     * Called when a select gesture is initiated.
     */
    private fun onSelectInitiated(event: MouseEvent) {
        selectionHelper.begin(makePoint(event.x, event.y))
    }

    /**
     * Called while a select gesture is in progress and the selection is changing.
     */
    private fun onSelecting(event: MouseEvent) {
        selectionHelper.let { helper: SelectionHelper<T, P> ->
            helper.update(makePoint(event.x, event.y))
            listOf(
                selector.createDrawCommand(
                    helper.rectangle,
                    Colors.selectionBox
                )
            ).let { drawCommands ->
                feedback = feedback + (Interaction.SELECTION_BOX to drawCommands)
            }
            addNodesToSelectionCandidates()
            repaint()
        }
        event.consume()
    }

    /**
     * Called when a select gesture finishes.
     */
    private fun onSelected(event: MouseEvent) {
        if (Keybinds[ActionFromKey.MODIFIER_CONTROL].filter { key: KeyCode -> keyboard.isHeld(key).not() }.isPresent) {
            selection.clear()
        }
        selectionHelper.clickSelection(nodes, wormhole)?.let { clickedNode: Pair<Node<T>, P> ->
            if (clickedNode.first in selection) {
                selection -= clickedNode.first
            } else {
                selection[clickedNode.first] = clickedNode.second
            }
        }
        selectionHelper.boxSelection(nodes, wormhole).let { boxedNodes: Map<Node<T>, P> ->
            boxedNodes.forEach { node: Map.Entry<Node<T>, P> ->
                if (node.key in selection) {
                    selection -= node.key
                } else {
                    selection[node.key] = node.value
                }
            }
        }
        selectionCandidatesMutex.acquireUninterruptibly()
        selectionHelper.close()
        selectionCandidates.clear()
        selectionCandidatesMutex.release()
        feedback = feedback - Interaction.SELECTION_BOX
        repaint()
        event.consume()
    }

    /**
     * Called when a select gesture is canceled.
     */
    private fun onSelectCanceled(event: MouseEvent) {
        selectionHelper.close()
        feedback = feedback - Interaction.SELECTION_BOX
        feedback = feedback - Interaction.HIGHLIGHT_CANDIDATE
        repaint()
        event.consume()
    }

    /**
     * Returns a lambda which paints a highlight when called.
     * @param position the position of the highlight
     * @param paint the colour of the highlight
     */
    private fun paintHighlight(position: P, paint: Paint): () -> Unit = {
        highlighter.graphicsContext2D.let { graphics ->
            graphics.fill = paint
            wormhole.getViewPoint(position).let {
                graphics.fillOval(
                    it.x - highlightSize / 2,
                    it.y - highlightSize / 2,
                    highlightSize,
                    highlightSize
                )
            }
        }
    }

    /**
     * Grabs all the nodes in the selection box and adds them to the selection candidates.
     */
    private fun addNodesToSelectionCandidates() {
        selectionCandidatesMutex.acquireUninterruptibly()
        selectionHelper.boxSelection(nodes, wormhole).let {
            if (it.entries != selectionCandidates.entries) {
                selectionCandidates.clear()
                selectionCandidates += it
            }
        }
        selectionCandidatesMutex.release()
    }

    /**
     * Clears and then paints every currently active feedback.
     */
    fun repaint() {
        Platform.runLater {
            selector.clear()
            highlighter.clear()
            feedback.values.forEach { it.forEach { it() } }
        }
    }

    /**
     * Sets the wormhole.
     */
    fun setWormhole(wormhole: BidimensionalWormhole<P>) {
        this.wormhole = wormhole
    }

    /**
     * Sets the zoom manager.
     */
    fun setZoomManager(zoomManager: ZoomManager) {
        this.zoomManager = zoomManager
    }

    /**
     * To be called by the [AbstractFXDisplay] whenever a step occurs.
     */
    fun simulationStep() {
        addNodesToSelectionCandidates()
    }

    companion object {
        /**
         * The size (radius) of the highlights.
         */
        const val highlightSize = 10.0
        /**
         * Empiric zoom scale value.
         */
        private const val ZOOM_SCALE = 40.0
    }

    private class Colors private constructor() {
        companion object {
            /**
             * The colour of the highlights for the already selected nodes.
             */
            val alreadySelected = "#1f70f2".color()
            /**
             * The colour of the highlights for the nodes that are candidates for selection.
             */
            val selecting = "#ff5400".color()
            /**
             *
             */
            val selectionBox = "#8e99f3".color()

            private fun String.color(): Paint = Color.valueOf(this)
        }
    }

    private class Alphas private constructor() {
        companion object {
            const val highlight = 0.5
            const val selection = 0.4
        }
    }
}
