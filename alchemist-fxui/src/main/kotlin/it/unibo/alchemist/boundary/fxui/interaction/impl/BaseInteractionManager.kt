/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.impl

import com.google.common.collect.ImmutableSet
import it.unibo.alchemist.boundary.fxui.impl.AbstractFXDisplay
import it.unibo.alchemist.boundary.fxui.interaction.api.Direction2D
import it.unibo.alchemist.boundary.fxui.interaction.api.InteractionManager
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.api.KeyboardActionListener
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.impl.ActionOnKey
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.impl.KeyboardEventDispatcher
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.impl.SimpleKeyboardEventDispatcher
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.util.ActionFromKey
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.util.Keybinds
import it.unibo.alchemist.boundary.fxui.interaction.mouse.api.ActionOnMouse
import it.unibo.alchemist.boundary.fxui.interaction.mouse.impl.DynamicMouseEventDispatcher
import it.unibo.alchemist.boundary.fxui.interaction.mouse.impl.MouseButtonTriggerAction
import it.unibo.alchemist.boundary.fxui.interaction.mouse.impl.NodeBoundMouseEventDispatcher
import it.unibo.alchemist.boundary.fxui.monitors.api.FXOutputMonitor
import it.unibo.alchemist.boundary.fxui.util.CanvasExtension.clear
import it.unibo.alchemist.boundary.fxui.util.CanvasExtension.createDrawRectangleCommand
import it.unibo.alchemist.boundary.fxui.util.PointExtension.makePoint
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.boundary.ui.api.ZoomManager
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import java.util.concurrent.Semaphore
import kotlin.math.roundToInt

/**
 * An interaction manager that implements pan, select, move, delete and zoom.
 * @param monitor the monitor.
 */
class BaseInteractionManager<T, P : Position2D<P>>(
    private val monitor: AbstractFXDisplay<T, P>
) : InteractionManager<T, P> {
    /**
     * Describes a certain interaction that has a feedback associated to it.
     */
    private enum class Interaction {
        HIGHLIGHT_CANDIDATE,
        HIGHLIGHTED,
        SELECTION_BOX
    }

    override lateinit var environment: Environment<T, P>
    override var nodes: Map<Node<T>, P> = emptyMap()
    override val keyboardListener: KeyboardActionListener
        get() = keyboard.listener

    private val highlighter = Canvas()
    private val selector = Canvas()
    override val canvases = Group().apply { listOf(highlighter, selector).forEach { children.add(it) } }

    private lateinit var wormhole: Wormhole2D<P>
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
    private val selectionHelper: SelectionHelper<T, P> = SelectionHelper()
    private var selection: Set<Node<T>> = emptySet()
    private var selectionCandidates: Set<Node<T>> = emptySet()
    private val selectedElements: ImmutableSet<Node<T>>
        get() = ImmutableSet.copyOf(selection)
    private val runMutex: Semaphore = Semaphore(1)

    /**
     * Once a new feedback is added, it should be rendered as quickly as possible.
     * For this reason, [repaint] is called whenever a new feedback is added to the map.
     * [Volatile] ensures that the JavaFX thread will see the changes to [feedback]
     * as soon as possible.
     */
    @Volatile private var feedback: Map<Interaction, List<() -> Unit>> = emptyMap()

    /**
     * Wraps the given command on the simulation to run through [Simulation.schedule]
     * in order to properly run the call on the simulation.
     *
     * For instance, deleting a node:
     * <code>invokeOnSimulation { environment.removeNode(someNode) }</code>
     */
    private val invokeOnSimulation: (Simulation<T, P>.() -> Unit) -> Unit
        get() = environment.simulation
            ?.let { { exec: Simulation<T, P>.() -> Unit -> it.schedule { exec.invoke(it) } } }
            ?: error("Uninitialized environment or simulation")

    init {
        listOf(selector, highlighter).forEach {
            it.widthProperty().bind(monitor.widthProperty())
            it.heightProperty().bind(monitor.heightProperty())
            it.isMouseTransparent = true
        }
        highlighter.graphicsContext2D.globalAlpha = Alphas.highlight
        selector.graphicsContext2D.globalAlpha = Alphas.selection
        // delete
        Keybinds[ActionFromKey.DELETE].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { deleteNodes() }
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
        Keybinds[ActionFromKey.MOVE].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { enqueueMove() }
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.CLICKED, MouseButton.MIDDLE)] = { enqueueMove() }
        // forward one step
        Keybinds[ActionFromKey.ONE_STEP].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { stepForward() }
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
                repaint()
                it.consume()
            }
        }
    }

    override fun repaint() {
        Platform.runLater {
            selector.clear()
            highlighter.clear()
            feedback.values.forEach { it.forEach { it() } }
        }
    }

    override fun setWormhole(wormhole: Wormhole2D<P>) {
        this.wormhole = wormhole
    }

    override fun setZoomManager(zoomManager: ZoomManager) {
        this.zoomManager = zoomManager
    }

    override fun onMonitorRepaint() {
        if (selectionHelper.isBoxSelectionInProgress) {
            addNodesToSelectionCandidates()
        }
        repaint()
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
            repaint()
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
        selectionHelper.update(makePoint(event.x, event.y))
        listOf(selector.createDrawRectangleCommand(selectionHelper.rectangle, Colors.selectionBox))
            .let { drawCommands -> feedback = feedback + (Interaction.SELECTION_BOX to drawCommands) }
        addNodesToSelectionCandidates()
        repaint()
        event.consume()
    }

    /**
     * Called when a select gesture finishes.
     */
    private fun onSelected(event: MouseEvent) {
        if (Keybinds[ActionFromKey.MODIFIER_CONTROL].filter { key: KeyCode -> keyboard.isHeld(key).not() }.isPresent) {
            selection = emptySet()
        }
        var selectedNodes = selectionHelper.boxSelection(nodes, wormhole).keys
        selectionHelper.clickSelection(nodes, wormhole)?.first?.let {
            selectedNodes = selectedNodes.plusElement(it)
        }
        selectionHelper.close()
        selectedNodes.filter { it !in selection }.let {
            selection += it
            selectedNodes -= it
        }
        selection -= selectedNodes.filter { it in selection }
        selectionCandidates = emptySet()
        feedback = feedback + (Interaction.HIGHLIGHTED to selection.map { paintHighlight(it, Colors.alreadySelected) })
        feedback = feedback - Interaction.SELECTION_BOX
        feedback = feedback - Interaction.HIGHLIGHT_CANDIDATE
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
     * @param node the node to highlight
     * @param paint the colour of the highlight
     */
    private fun paintHighlight(node: Node<T>, paint: Paint): () -> Unit = {
        highlighter.graphicsContext2D.let { graphics ->
            graphics.fill = paint
            nodes[node]?.run(wormhole::getViewPoint)?.let {
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
        selectionHelper.boxSelection(nodes, wormhole).keys.let { nodes ->
            if (nodes != selectionCandidates) {
                selectionCandidates = nodes
                feedback = feedback + (
                    Interaction.HIGHLIGHT_CANDIDATE to
                        selectionCandidates.map { paintHighlight(it, Colors.selecting) }
                    )
            }
        }
    }

    private fun enqueueMove() {
        mouse.setDynamicAction(MouseButtonTriggerAction(ActionOnMouse.CLICKED, MouseButton.PRIMARY)) { mouse ->
            runMutex.acquireUninterruptibly()
            if (selection.isNotEmpty()) {
                val nodesToMove: Set<Node<T>> = ImmutableSet.copyOf(selectedElements)
                val mousePosition = wormhole.getEnvPoint(makePoint(mouse.x, mouse.y))
                selection = emptySet()
                feedback = feedback - Interaction.HIGHLIGHTED
                nodesToMove
                    .mapNotNull { nodes[it] }
                    .maxWithOrNull { a, b -> (b - a.coordinates).run { x + y }.roundToInt() }
                    ?.run { (mousePosition - coordinates).coordinates }
                    ?.let { offset ->
                        invokeOnSimulation {
                            environment::moveNodeToPosition.let { move ->
                                nodes
                                    .filterKeys { it in nodesToMove }
                                    .mapValues { it.value + offset }
                                    .forEach { move(it.key, it.value) }
                            }
                        }
                    }
            }
            runMutex.release()
        }
    }

    private fun deleteNodes() {
        runMutex.acquireUninterruptibly()
        if (selection.isNotEmpty()) {
            val nodesToRemove = ImmutableSet.copyOf(selectedElements)
            selection = emptySet()
            feedback = feedback - Interaction.HIGHLIGHTED
            invokeOnSimulation {
                nodesToRemove.forEach { environment?.removeNode(it) }
            }
            repaint()
        }
        runMutex.release()
    }

    private fun stepForward() {
        invokeOnSimulation {
            if (status == Status.PAUSED) {
                goToStep(step + 1)
            }
        }
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

    private object Colors {
        /**
         * The colour of the highlights for the already selected nodes.
         */
        val alreadySelected = "#1f70f2".toColor()
        /**
         * The colour of the highlights for the nodes that are candidates for selection.
         */
        val selecting = "#ff5400".toColor()
        /**
         *
         */
        val selectionBox = "#8e99f3".toColor()

        private fun String.toColor(): Paint = Color.valueOf(this)
    }

    private object Alphas {
        const val highlight = 0.5
        const val selection = 0.4
    }
}
