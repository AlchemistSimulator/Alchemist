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
import it.unibo.alchemist.kotlin.distanceTo
import it.unibo.alchemist.kotlin.makePoint
import it.unibo.alchemist.kotlin.plus
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
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
    var nodes: Map<Node<T>, Position2D<*>> = emptyMap()

    private val input = Canvas()
    private val highlighter = Canvas()
    private val selector = Canvas()
    private val zoomManager: ZoomManager by lazy {
        ExponentialZoomManager(this.wormhole.zoom, ExponentialZoomManager.DEF_BASE)
    }
    private val selection: ObservableMap<Node<T>, Position2D<*>> = FXCollections.observableHashMap()
    private val selectionCandidates: ObservableMap<Node<T>, Position2D<*>> = FXCollections.observableHashMap()
    private val candidatesMutex: Semaphore = Semaphore(1)

    private var keyboardModifiers: Set<FXOutputMonitor.KeyboardModifier> = emptySet()
    private var panPosition: Point? = null
    private var selectionBox: SelectionBox<T>? = null
    private var selectionPoint: Point? = null
    private var isSelecting = false
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

        input.setOnScroll {
            zoomManager.inc(it.deltaY / ZOOM_SCALE)
            wormhole.zoomOnPoint(makePoint(it.x, it.y), zoomManager.zoom)
            parentMonitor.repaint()
            it.consume()
        }
        input.setOnMousePressed {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanInitiated(it)
                FXOutputMonitor.ViewStatus.SELECTING -> {
                        selectionPoint = makePoint(it.x, it.y)
                        isSelecting = true
                        it.consume()
                    }
            }
        }
        input.setOnMouseDragged {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanning(it)
                FXOutputMonitor.ViewStatus.SELECTING -> {
                    if (isSelecting) {
                        if (selectionBox == null) {
                            onBoxSelectInitiated(it)
                        } else {
                            onBoxSelecting(it)
                        }
                    }
                }
            }
        }
        input.setOnMouseReleased {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanned(it)
                FXOutputMonitor.ViewStatus.SELECTING -> {
                    if (isSelecting) {
                        onSelected(it)
                        isSelecting = false
                    }
                    it.consume()
                }
            }
        }
        input.setOnMouseExited {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanCanceled(it)
                FXOutputMonitor.ViewStatus.SELECTING -> {
                    isSelecting = false
                    onSelectCanceled(it)
                }
            }
        }

        selection.addListener(MapChangeListener {
            feedback += Interaction.HIGHLIGHTED to selection.map { paintHighlight(it.value, alreadySelected.color()) }
            repaint()
        })
        selectionCandidates.addListener(MapChangeListener {
            feedback += Interaction.HIGHLIGHT_CANDIDATE to selectionCandidates.map { paintHighlight(it.value, selecting.color()) }
            repaint()
        })
    }

    /**
     * Called when a pan gesture is initiated.
     * @param event the caller
     */
    private fun onPanInitiated(event: MouseEvent) {
        panPosition = makePoint(event.x, event.y)
    }

    /**
     * Called while a pan gesture is in progress and the view is moving.
     * @param event the caller
     */
    private fun onPanning(event: MouseEvent) {
        panPosition?.also { previousPan ->
            val currentPoint = makePoint(event.x, event.y)
            val newPoint = wormhole.viewPosition + makePoint(
                currentPoint.x - previousPan.getX(),
                currentPoint.y - previousPan.getY()
            )
            wormhole.viewPosition = newPoint
            panPosition = currentPoint
            parentMonitor.repaint()
        }
        event.consume()
    }

    /**
     * Called when a pan gesture finishes.
     */
    private fun onPanned(event: MouseEvent) {
        panPosition = null
        event.consume()
    }

    /**
     * Called when a pan gesture is canceled.
     */
    private fun onPanCanceled(event: MouseEvent) {
        panPosition = null
        event.consume()
    }

    /**
     * Called when a select gesture is initiated
     */
    private fun onBoxSelectInitiated(event: MouseEvent) {
        selectionPoint?.let { selectionBox = SelectionBox(it, selector.graphicsContext2D, wormhole) }
        event.consume()
    }

    /**
     * Called while a select gesture is in progress and the selection is changing.
     */
    private fun onBoxSelecting(event: MouseEvent) {
        selectionBox?.let {
            feedback += Interaction.SELECTION_BOX to listOf(it.update(makePoint(event.x, event.y)))
            addNodesToSelectionCandidates()
            repaint()
        }
        event.consume()
    }

    /**
     * Called when a select gesture finishes.
     */
    private fun onSelected(event: MouseEvent) {
        if (selectionBox == null) { // a single node has been selected (click selection)
            wormhole.getEnvPoint(selectionPoint).let { cursorPosition ->
                nodes.keys.minBy { (nodes[it]!!).distanceTo(cursorPosition) }?.let {
                    if (!keyboardModifiers.contains(FXOutputMonitor.KeyboardModifier.CTRL)) {
                        selection.clear()
                        selection[it] = nodes[it]
                    } else {
                        if (it in selection) {
                            selection -= it
                        } else {
                            selection[it] = nodes[it]
                        }
                    }
                }
            }
        } else { // multiple nodes have been selected (box selection)
            if (!keyboardModifiers.contains(FXOutputMonitor.KeyboardModifier.CTRL)) {
                selection.clear()
                selectionBox?.let {
                    selection += it.finalize(nodes)
                }
            } else {
                selectionBox?.let { it.finalize(nodes).let {
                        it.forEach {
                            if (it.key in selection) {
                                selection -= it.key
                            } else {
                                selection[it.key] = it.value
                            }
                        }
                    }
                }
            }
            selectionBox = null
            candidatesMutex.acquireUninterruptibly()
            selectionCandidates.clear()
            candidatesMutex.release()
        }
        feedback += Interaction.SELECTION_BOX to emptyList()
        selectionPoint = null
        repaint()
        event.consume()
    }

    /**
     * Called when a select gesture is canceled.
     */
    private fun onSelectCanceled(event: MouseEvent) {
        if (selectionBox != null) {
            selectionBox = null
            feedback += Interaction.SELECTION_BOX to emptyList()
            feedback += Interaction.HIGHLIGHT_CANDIDATE to emptyList()
            repaint()
        }
        selectionPoint = null
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
        selectionBox?.intersectingNodes(nodes)?.let {
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
     * Toggles a given key modifier.
     */
    fun toggleModifier(modifier: FXOutputMonitor.KeyboardModifier) {
        if (keyboardModifiers.contains(modifier)) {
            keyboardModifiers -= modifier
        } else {
            keyboardModifiers += modifier
        }
    }

    /**
     * Returns the canvases used by this InteractionManager.
     */
    fun canvases(): List<Canvas> = listOf(input, highlighter, selector)

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
        private const val GET_X_METHOD_NAME = "getX"
        private const val GET_Y_METHOD_NAME = "getY"
        private const val highlightSize = 10.0
        private const val alreadySelected = "#1f70f2"
        private const val selecting = "#ff5400"
        /**
         * Empiric zoom scale value.
         */
        private const val ZOOM_SCALE = 40.0
    }
}

/**
 * Allows basic multi-element box selections.
 * @param T the concentration of the simulation
 * @param anchorPoint the starting and unchanging [Point] of the selection
 * @param context the [GraphicsContext] used for printing the visual representation of the selection
 * @param wormhole the wormhole of the display being used
 */
class SelectionBox<T>(private val anchorPoint: Point, private val context: GraphicsContext, private val wormhole: BidimensionalWormhole<Position2D<*>>) {
    private val movingPoint = Point(anchorPoint.x, anchorPoint.y)
    var elements: Map<Node<T>, Position2D<*>>? = null
        private set
    private var rectangle = Rectangle()
        get() = anchorPoint.makeRectangleWith(movingPoint)

    /**
     * Updates the box, clearing the old one and drawing the updated one.
     * @param newPoint the cursor's new position
     */
    fun update(newPoint: Point): () -> Unit = checkFinalized().setNewPoint(newPoint).draw()

    /**
     * Returns the nodes currently intersecting with the selection-
     * @param nodes a map having nodes as keys and their positions as values
     */
    fun intersectingNodes(nodes: Map<Node<T>, Position2D<*>>): Map<Node<T>, Position2D<*>> =
//        checkFinalized().rectangle.let { area -> nodes.filterValues { wormhole.getViewPoint(it) in area } }
        if (elements != null) { emptyMap() } else /*checkFinalized().*/rectangle.let { area -> nodes.filterValues { wormhole.getViewPoint(it) in area } }

    /**
     * Locks the elements and writes the items selected to [elements].
     * @param nodes a map having nodes as keys and their positions as values
     */
    fun finalize(nodes: Map<Node<T>, Position2D<*>>): Map<Node<T>, Position2D<*>> =
        intersectingNodes(nodes).also { elements = it }

    /**
     * Returns a lambda that draws the box.
     */
    fun draw(): () -> Unit = { rectangle.let {
        // can edit the elements box's style here
        context.fill = selection.color()
        context.fillRect(it.x, it.y, it.width, it.height)
    } }

    /**
     * Returns a lambda that clears the box.
     */
    fun clear(): () -> Unit = { rectangle.let {
        context.clearRect(it.x, it.y, it.width, it.height)
    } }

    /**
     * Sets the moving point to the given point
     */
    private fun setNewPoint(point: Point): SelectionBox<T> = apply {
        movingPoint.x = point.x
        movingPoint.y = point.y
    }

    /**
     * Checks if the selection has been finalized
     * @throws IllegalStateException if the selection has been finalized
     */
    private fun checkFinalized(): SelectionBox<T> {
        if (elements != null) {
            throw IllegalStateException("Selection " + this + " has already been finalized")
        }
        return this
    }

    companion object {
        private const val selection = "#8e99f3"
    }
}

private operator fun Rectangle.contains(point: Point): Boolean =
    point.x in this.x..(this.x + this.width) &&
        point.y in this.y..(this.y + this.height)

private fun Point.makeRectangleWith(other: Point): Rectangle = Rectangle(
    min(this.x, other.x).toDouble(),
    min(this.y, other.y).toDouble(),
    abs(this.x - other.x).toDouble(),
    abs(this.y - other.y).toDouble())

private fun String.color(): Paint = Color.valueOf(this)