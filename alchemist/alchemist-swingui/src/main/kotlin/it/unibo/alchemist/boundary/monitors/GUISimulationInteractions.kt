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
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.InputEvent
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import java.awt.Point
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Optional
import kotlin.math.abs
import kotlin.math.min

enum class Interaction {
    HIGHLIGHT_CANDIDATE,
    HIGHLIGHTED,
    SELECTION_BOX
}

/**
 * Manages interactions with the simulation (user input and relevant feedback)
 */
interface InteractionManager<T> {

    /**
     * Nodes currently selected by the user
     */
    val selectedElements: ImmutableMap<Node<T>, Position2D<*>>

    /**
     * Toggles a given keyboard modifier
     */
    fun toggleModifier(modifier: FXOutputMonitor.KeyboardModifier)

    /**
     * To be called whenever a step is done
     */
    fun simulationStep()

    /**
     * Sets the wormhole to be used to represent feedback
     *
     * TODO: In the constructor?
     */
    fun setWormhole(wormhole: BidimensionalWormhole<Position2D<*>>)

    /**
     * Returns the canvases used by this interaction manager
     */
    fun canvases(): Collection<Canvas>
}

/**
 * A basic interaction manager. Implements zoom, pan and select features.
 * @param nodes an observable map containing the nodes in the simulation
 * @param parentMonitor the parent monitor
 */
class SimpleInteractionManager<T>(
    private val nodes: ObservableMap<Node<T>, Position2D<*>>,
    private val parentMonitor: AbstractFXDisplay<T>
) : InteractionManager<T> {

    private val input = Canvas()
    private val highlighter = Canvas()
    private val selector = Canvas()

    private lateinit var wormhole: BidimensionalWormhole<Position2D<*>>
    private var panPosition: Position2D<*>? = null
    private var zoomManager: ZoomManager? = null
    private var selectionBox: SelectionBox<T>? = null
    private var selectionPoint: Point? = null
    private var isSelecting = false
    private val selection: ObservableMap<Node<T>, Position2D<*>> = FXCollections.observableHashMap()
    private val selectionCandidates: ObservableMap<Node<T>, Position2D<*>> = FXCollections.observableHashMap()
    @Volatile private var feedback: Map<Interaction, List<() -> Unit>> = emptyMap()
    private var keyboardModifiers: Set<FXOutputMonitor.KeyboardModifier> = emptySet()

    override val selectedElements: ImmutableMap<Node<T>, Position2D<*>>
        get() = ImmutableMap.copyOf(selection)

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
            if (zoomManager != null) {
                zoomManager!!.inc(it.deltaY / ZOOM_SCALE)
                wormhole.zoomOnPoint(makePoint(it.x, it.y), zoomManager!!.zoom)
            }
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
                else -> {
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
                else -> {
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
                else -> {
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
                else -> {
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
        getEventPosition(event).ifPresent { panPosition = it }
        event.consume()
    }

    /**
     * Called while a pan gesture is in progress and the view is moving.
     * @param event the caller
     */
    private fun onPanning(event: MouseEvent) {
        if (panPosition != null) {
            getEventPosition(event).ifPresent { mousePosition ->
                val currentPoint = wormhole.getViewPoint(mousePosition)
                val newPoint = wormhole.getViewPoint(panPosition!!).let { previousPoint ->
                    makePoint(
                        (wormhole.viewPosition.getX() + (currentPoint.getX() - previousPoint.getX())),
                        (wormhole.viewPosition.getY() + (currentPoint.getY() - previousPoint.getY()))) }
                wormhole.viewPosition = newPoint
                panPosition = wormhole.getEnvPoint(currentPoint)
                parentMonitor.repaint()
                repaint()
            }
        }
//        if (panPosition != null) {
//            getEventPosition(event).ifPresent {
//                val currentPoint = wormhole.getViewPoint(it)
//                val newPoint = wormhole.getViewPoint(panPosition!!).let {
//                    makePoint(
//                        (wormhole.viewPosition.getX() + (currentPoint.getX() - it.getX())),
//                        (wormhole.viewPosition.getY() + (currentPoint.getY() - it.getY()))) }
//                wormhole.viewPosition = newPoint
//                panPosition = wormhole.getEnvPoint(currentPoint)
//                parentMonitor.repaint()
//            }
//        }
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
        selectionBox = SelectionBox(selectionPoint!!, selector.graphicsContext2D, wormhole)
        event.consume()
    }

    /**
     * Called while a select gesture is in progress and the selection is changing.
     */
    private fun onBoxSelecting(event: MouseEvent) {
        feedback += Interaction.SELECTION_BOX to listOf(selectionBox!!.update(makePoint(event.x, event.y)))
        addNodesToSelectionCandidates()
        repaint()
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
                selection += selectionBox!!.finalize(nodes)
            } else {
                selectionBox!!.finalize(nodes).let {
                    it.forEach {
                        if (it.key in selection) {
                            selection -= it.key
                        } else {
                            selection[it.key] = it.value
                        }
                    }
                }
            }
            feedback += Interaction.SELECTION_BOX to emptyList()
            selectionCandidates.clear()
            selectionBox = null
        }
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
            repaint()
        }
        selectionPoint = null
        event.consume()
    }

    /**
     * The method returns the [Position] in the [Environment] of the given `Event`, if any.
     *
     * @param event the event to check
     * @return the position, if any
     */
    private fun getEventPosition(event: InputEvent): Optional<Position2D<*>> {
        val wormhole = wormhole
        val methods = event.javaClass.methods
        var getX = Optional.empty<Method>()
        var getY = Optional.empty<Method>()
        for (method in methods) {
            val modifier = method.modifiers
            if (Modifier.isPublic(modifier) && !Modifier.isAbstract(modifier)) {
                val name = method.name
                if (name == GET_X_METHOD_NAME) {
                    getX = Optional.of(method)
                } else if (name == GET_Y_METHOD_NAME) {
                    getY = Optional.of(method)
                }
                if (getX.isPresent && getY.isPresent) {
                    break
                }
            }
        }
        return if (getX.isPresent && getY.isPresent) {
            try {
                val x = getX.get().invoke(event) as Number
                val y = getY.get().invoke(event) as Number
                Optional.of(wormhole.getEnvPoint(makePoint(x, y)))
            } catch (e: Exception) {
                when (e) {
                    is IllegalAccessException,
                    is InvocationTargetException -> Optional.empty<Position2D<*>>()
                    else -> throw e
                }
            }
        } else {
            Optional.empty()
        }
    }

    private fun repaint() {
        Platform.runLater {
            clearCanvas(selector)
            clearCanvas(highlighter)
            feedback.values.forEach { it.forEach { it() } }
        }
    }

    private fun paintHighlight(position: Position2D<*>, paint: Paint): () -> Unit = {
        highlighter.graphicsContext2D.let { graphics ->
            graphics.fill = paint
            wormhole.getViewPoint(position).let {
                graphics.fillOval(it.x - highlightSize / 2, it.y - highlightSize / 2, highlightSize, highlightSize)
            }
        }
    }

    private fun clearCanvas(canvas: Canvas) {
        canvas.graphicsContext2D.clearRect(0.0, 0.0, canvas.width, canvas.height)
    }

    private fun addNodesToSelectionCandidates() {
        selectionBox?.intersectingNodes(nodes)?.let {
            if (it.entries != selectionCandidates.entries) {
                selectionCandidates.clear()
                selectionCandidates += it
            }
        }
    }

    override fun toggleModifier(modifier: FXOutputMonitor.KeyboardModifier) {
        if (keyboardModifiers.contains(modifier)) {
            keyboardModifiers -= modifier
        } else {
            keyboardModifiers += modifier
        }
    }

    override fun canvases(): Collection<Canvas> = listOf(input, highlighter, selector)

    override fun setWormhole(wormhole: BidimensionalWormhole<Position2D<*>>) {
        this.wormhole = wormhole
        zoomManager = ExponentialZoomManager(this.wormhole.zoom, ExponentialZoomManager.DEF_BASE)
    }

    override fun simulationStep() {
        addNodesToSelectionCandidates()
    }

    companion object {
        private const val GET_X_METHOD_NAME = "getX"
        private const val GET_Y_METHOD_NAME = "getY"
        private const val highlightSize = 10.0
        private const val alreadySelected = "#1f70f2"
        private const val selecting = "#fffa00"
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
        checkFinalized().rectangle.let { area -> nodes.filterValues { wormhole.getViewPoint(it) in area } }

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