/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import com.google.common.collect.ImmutableMap
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor
import it.unibo.alchemist.boundary.wormhole.implementation.ExponentialZoomManager
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager
import it.unibo.alchemist.input.ActionOnKey
import it.unibo.alchemist.input.ActionOnMouse
import it.unibo.alchemist.input.CanvasBoundMouseEventDispatcher
import it.unibo.alchemist.input.KeyboardActionListener
import it.unibo.alchemist.input.KeyboardTriggerAction
import it.unibo.alchemist.input.MouseTriggerAction
import it.unibo.alchemist.input.SimpleKeyboardEventDispatcher
import it.unibo.alchemist.kotlin.distanceTo
import it.unibo.alchemist.kotlin.makePoint
import it.unibo.alchemist.kotlin.plus
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import java.awt.Point
import java.util.concurrent.Semaphore
import kotlin.math.abs
import kotlin.math.min

enum class Interaction {
    HIGHLIGHT_CANDIDATE,
    HIGHLIGHTED,
    SELECTION_BOX
}

/**
 * A basic interaction manager. Implements zoom, pan and select features.
 * @param nodes an observable map containing the nodes in the simulation
 * @param parentMonitor the parent monitor
 */
class InteractionManager<T>(
    private val parentMonitor: AbstractFXDisplay<T>
) {

    val selectedElements: ImmutableMap<Node<T>, Position2D<*>>
        get() = ImmutableMap.copyOf(selection)
    /**
     * The nodes in the environment.
     * Should be updated as frequently as possible to ensure a consistent representation of the feedback.
     */
    var nodes: Map<Node<T>, Position2D<*>> = emptyMap()
    /**
     * The keyboard listener.
     */
    val keyboardListener: KeyboardActionListener
        get() = keyboard.listener
    /**
     * The canvases used for input/output.
     */
    val canvases: List<Canvas>
        get() = listOf(input, highlighter, selector)
    /**
     * Commands that have to be run through the environment.
     */
    var runOnSimulation: List<(env: Environment<T, Position2D<*>>) -> Unit> = emptyList()
    /**
     * Mutex for the commands that have to be run through the environment
     */
    val runMutex: Semaphore = Semaphore(1)

    private val input = Canvas()
    private val highlighter = Canvas()
    private val selector = Canvas()
    private val keyboard = SimpleKeyboardEventDispatcher()
    private val mouse = CanvasBoundMouseEventDispatcher(input)
    private val zoomManager: ZoomManager by lazy {
        ExponentialZoomManager(this.wormhole.zoom, ExponentialZoomManager.DEF_BASE)
    }
    private val selection: ObservableMap<Node<T>, Position2D<*>> = FXCollections.observableHashMap()
    private val selectionCandidates: ObservableMap<Node<T>, Position2D<*>> = FXCollections.observableHashMap()
    private val candidatesMutex: Semaphore = Semaphore(1)
    private lateinit var panHelper: PanHelper
    private val selectionHelper: SelectionHelper<T> = SelectionHelper()
    private lateinit var wormhole: BidimensionalWormhole<Position2D<*>>
    @Volatile private var feedback: Map<Interaction, List<() -> Unit>> = emptyMap()

    init {
        input.apply {
            widthProperty().bind(parentMonitor.widthProperty())
            heightProperty().bind(parentMonitor.heightProperty())
        }
        selector.apply {
            widthProperty().bind(parentMonitor.widthProperty())
            heightProperty().bind(parentMonitor.heightProperty())
            isMouseTransparent = true
        }
        highlighter.apply {
            widthProperty().bind(parentMonitor.widthProperty())
            heightProperty().bind(parentMonitor.heightProperty())
            isMouseTransparent = true
        }

        highlighter.graphicsContext2D.globalAlpha = 0.5
        selector.graphicsContext2D.globalAlpha = 0.4

        // TODO: rework node deletion. the way delete works now, it only updates when a step occurs.
        val deleteNodes: (KeyEvent) -> Unit = {
            runMutex.acquireUninterruptibly()
            if (selection.isNotEmpty()) {
                val nodesToRemove: Set<Node<T>> = selection.keys
                runOnSimulation += { env ->
                    nodesToRemove.forEach { env.removeNode(it) }
                    repaint()
                }
            }
            runMutex.release()
        }
        keyboard.setOnAction(KeyboardTriggerAction(ActionOnKey.PRESSED, KeyCode.DELETE), deleteNodes)
        keyboard.setOnAction(KeyboardTriggerAction(ActionOnKey.PRESSED, KeyCode.BACK_SPACE), deleteNodes)

        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.PRESSED, MouseButton.PRIMARY)) {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanInitiated(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectInitiated(it)
            }
        }
        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.DRAGGED, MouseButton.PRIMARY)) {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanning(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelecting(it)
            }
        }
        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.RELEASED, MouseButton.PRIMARY)) {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanned(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelected(it)
            }
        }
        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.EXITED, MouseButton.PRIMARY)) {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanCanceled(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectCanceled(it)
            }
        }
        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.PRESSED, MouseButton.SECONDARY), this::onSelectInitiated)
        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.DRAGGED, MouseButton.SECONDARY), this::onSelecting)
        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.RELEASED, MouseButton.SECONDARY), this::onSelected)
        mouse.setOnAction(MouseTriggerAction(ActionOnMouse.EXITED, MouseButton.SECONDARY), this::onSelectCanceled)

        input.setOnScroll {
            zoomManager.inc(it.deltaY / ZOOM_SCALE)
            wormhole.zoomOnPoint(makePoint(it.x, it.y), zoomManager.zoom)
            parentMonitor.repaint()
            it.consume()
        }

        selection.addListener(MapChangeListener {
            feedback += Interaction.HIGHLIGHTED to selection.map { paintHighlight(it.value, alreadySelectedColour.color()) }
            repaint()
        })
        selectionCandidates.addListener(MapChangeListener {
            feedback += Interaction.HIGHLIGHT_CANDIDATE to selectionCandidates.map { paintHighlight(it.value, selectingColour.color()) }
            repaint()
        })
    }

    /**
     * Called when a pan gesture is initiated.
     * @param event the caller
     */
    private fun onPanInitiated(event: MouseEvent) {
        panHelper = PanHelper(makePoint(event.x, event.y))
    }

    /**
     * Called while a pan gesture is in progress and the view is moving.
     * @param event the caller
     */
    private fun onPanning(event: MouseEvent) {
        if (panHelper.valid) {
            wormhole.viewPosition = panHelper.update(makePoint(event.x, event.y), wormhole.viewPosition)
            parentMonitor.repaint()
        }
        event.consume()
    }

    /**
     * Called when a pan gesture finishes.
     */
    private fun onPanned(event: MouseEvent) {
        panHelper.close()
        event.consume()
    }

    /**
     * Called when a pan gesture is canceled.
     */
    private fun onPanCanceled(event: MouseEvent) {
        panHelper.close()
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
        selectionHelper.let {
            it.update(makePoint(event.x, event.y))
            feedback += Interaction.SELECTION_BOX to listOf(selector.createDrawCommand(it.rectangle))
            addNodesToSelectionCandidates()
            repaint()
        }
        event.consume()
    }

    /**
     * Called when a select gesture finishes.
     */
    private fun onSelected(event: MouseEvent) {
        if (!keyboard.isHeld(KeyCode.CONTROL)) {
            selection.clear()
        }
        selectionHelper.clickSelection(nodes, wormhole)?.let {
            if (it.first in selection) {
                selection -= it.first
            } else {
                selection[it.first] = it.second
            }
        }
        selectionHelper.boxSelection(nodes, wormhole).let {
            it.forEach {
                if (it.key in selection) {
                    selection -= it.key
                } else {
                    selection[it.key] = it.value
                }
            }
        }
        candidatesMutex.acquireUninterruptibly()
        selectionHelper.close()
        selectionCandidates.clear()
        candidatesMutex.release()
        feedback += Interaction.SELECTION_BOX to emptyList()
        repaint()
        event.consume()
    }

    /**
     * Called when a select gesture is canceled.
     */
    private fun onSelectCanceled(event: MouseEvent) {
        selectionHelper.close()
        feedback += Interaction.SELECTION_BOX to emptyList()
        feedback += Interaction.HIGHLIGHT_CANDIDATE to emptyList()
        repaint()
        event.consume()
    }

    /**
     * Returns a lambda which paints a highlight when called.
     * @param position the position of the highlight
     * @param paint the colour of the highlight
     */
    private fun paintHighlight(position: Position2D<*>, paint: Paint): () -> Unit = {
        highlighter.graphicsContext2D.let { graphics ->
            graphics.fill = paint
            wormhole.getViewPoint(position).let {
                graphics.fillOval(it.x - highlightSize / 2, it.y - highlightSize / 2, highlightSize, highlightSize)
            }
        }
    }

    /**
     * Clears a given canvas.
     */
    private fun clearCanvas(canvas: Canvas) {
        canvas.graphicsContext2D.clearRect(0.0, 0.0, canvas.width, canvas.height)
    }

    /**
     * Grabs all the nodes in the selection box and adds them to the selection candidates.
     */
    private fun addNodesToSelectionCandidates() {
        candidatesMutex.acquireUninterruptibly()
        selectionHelper.boxSelection(nodes, wormhole).let {
            if (it.entries != selectionCandidates.entries) {
                selectionCandidates.clear()
                selectionCandidates += it
            }
        }
        candidatesMutex.release()
    }

    /**
     * Clears and then paints every currently active feedback.
     */
    fun repaint() {
        Platform.runLater {
            clearCanvas(selector)
            clearCanvas(highlighter)
            feedback.values.forEach { it.forEach { it() } }
        }
    }

    /**
     * Sets the wormhole.
     */
    fun setWormhole(wormhole: BidimensionalWormhole<Position2D<*>>) {
        this.wormhole = wormhole
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
         * The colour of the highlights for the already selected nodes.
         */
        const val alreadySelectedColour = "#1f70f2"
        /**
         * The colour of the highlights for the nodes that are candidates for selection.
         */
        const val selectingColour = "#ff5400"
        const val selectionBoxColour = "#8e99f3"
        /**
         * Empiric zoom scale value.
         */
        private const val ZOOM_SCALE = 40.0
    }
}

/**
 * Manages panning.
 */
class PanHelper(private var panPosition: Point) {

    var valid: Boolean = true
        private set

    /**
     * Updates the panning position and returns it.
     * @param currentPoint the destination point
     * @param viewPoint the position of the view
     */
    fun update(currentPoint: Point, viewPoint: Point): Point = if (valid) {
        panPosition.let { previousPan ->
            viewPoint + makePoint(
                currentPoint.x - previousPan.getX(),
                currentPoint.y - previousPan.getY()
            ).also {
                panPosition = currentPoint
            }
        }
    }
    else {
        throw IllegalStateException("Unable to pan after finalizing the PanHelper")
    }

    /**
     * Closes the helper.
     */
    fun close() {
        valid = false
    }
}

/**
 * Manages multi-element selection and click-selection.
 */
class SelectionHelper<T> {

    /**
     * Allows basic multi-element box selections.
     * @param anchorPoint the starting and unchanging [Point] of the selection
     */
    class SelectionBox(val anchorPoint: Point, val movingPoint: Point = anchorPoint) {
        var rectangle = Rectangle()
            get() = anchorPoint.makeRectangleWith(movingPoint)

        override fun toString(): String = "[$anchorPoint, $movingPoint]"
    }

    private var box: SelectionBox? = null
    private var selectionPoint: Point? = null
    private var isSelecting = false

    /**
     * The rectangle representing the box.
     * If the rectangle's dimensions are (0, 0), the rectangle is to be considered non-existing.
     */
    val rectangle
        get() = box?.rectangle ?: makePoint(0, 0).let { it.makeRectangleWith(it) }

    /**
     * Begins a new selection at the given point.
     */
    fun begin(point: Point) : SelectionHelper<T> = apply {
        isSelecting = true
        selectionPoint = point
        box = SelectionBox(point)
    }

    /**
     * Updates the selection with a new point.
     */
    fun update(point: Point) : SelectionHelper<T> = apply {
        if (isSelecting) {
            box?.let {
                box = SelectionBox(it.anchorPoint, point)
            }
            selectionPoint = null
        }
    }

    /**
     * Closes the selection.
     */
    fun close() {
        box = null
        selectionPoint = null
        isSelecting = false
    }

    /**
     * Retrieves the element selected by clicking. If selection was not done by clicking, null
     */
    fun clickSelection(nodes: Map<Node<T>, Position2D<*>>,
        wormhole: BidimensionalWormhole<Position2D<*>>): Pair<Node<T>, Position2D<*>>? =
        selectionPoint?.let { point ->
            nodes.minBy { (nodes[it.key]!!).distanceTo(wormhole.getEnvPoint(point)) }?.let {
                Pair(it.key, it.value)
            }
        }

    /**
     * Retrieves the elements selected by box selection, thus possibly empty
     */
    fun boxSelection(nodes: Map<Node<T>, Position2D<*>>,
        wormhole: BidimensionalWormhole<Position2D<*>>): Map<Node<T>, Position2D<*>> =
        box?.let {
            rectangle.intersectingNodes(nodes, wormhole)
        } ?: emptyMap()
}

/**
 * Returns a command for drawing the given rectangle on the caller canvas.
 */
private fun Canvas.createDrawCommand(rectangle: Rectangle): () -> Unit = {
    graphicsContext2D.let {
        it.fill = InteractionManager.selectionBoxColour.color()
        it.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
    } }

/**
 * Returns the nodes intersecting with the caller rectangle.
 */
private fun <T> Rectangle.intersectingNodes(
    nodes: Map<Node<T>, Position2D<*>>,
    wormhole: BidimensionalWormhole<Position2D<*>>): Map<Node<T>, Position2D<*>> =
    let { area -> nodes.filterValues { wormhole.getViewPoint(it) in area } }

private operator fun Rectangle.contains(point: Point): Boolean =
    point.x in x..(x + width) &&
        point.y in y..(y + height)

private fun Point.makeRectangleWith(other: Point): Rectangle = Rectangle(
    min(this.x, other.x).toDouble(),
    min(this.y, other.y).toDouble(),
    abs(this.x - other.x).toDouble(),
    abs(this.y - other.y).toDouble())

private fun String.color(): Paint = Color.valueOf(this)