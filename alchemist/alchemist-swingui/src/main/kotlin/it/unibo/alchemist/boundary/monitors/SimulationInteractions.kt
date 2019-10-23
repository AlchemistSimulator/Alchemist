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
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.input.ActionFromKey
import it.unibo.alchemist.input.ActionOnKey
import it.unibo.alchemist.input.ActionOnMouse
import it.unibo.alchemist.input.CanvasBoundMouseEventDispatcher
import it.unibo.alchemist.input.Keybinds
import it.unibo.alchemist.input.KeyboardActionListener
import it.unibo.alchemist.input.KeyboardEventDispatcher
import it.unibo.alchemist.input.MouseButtonTriggerAction
import it.unibo.alchemist.input.SimpleKeyboardEventDispatcher
import it.unibo.alchemist.input.TemporariesMouseEventDispatcher
import it.unibo.alchemist.kotlin.makePoint
import it.unibo.alchemist.kotlin.minus
import it.unibo.alchemist.kotlin.plus
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import javafx.event.Event
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import java.awt.Point
import java.util.Timer
import java.util.concurrent.Semaphore
import kotlin.concurrent.fixedRateTimer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * An interaction manager that controls the input/output on the environment done through the GUI.
 * @param parentMonitor the parent monitor
 */
class InteractionManager<T, P : Position2D<P>>(
    private val parentMonitor: AbstractFXDisplay<T, P>
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
     * The canvases used for input/output.
     */
    val canvases: List<Canvas>
        get() = listOf(input, highlighter, selector)
    /**
     * The keyboard listener.
     */
    val keyboardListener: KeyboardActionListener
        get() = keyboard.listener

    private lateinit var wormhole: BidimensionalWormhole<P>
    private val input: Canvas = Canvas()
    private val keyboard: KeyboardEventDispatcher = SimpleKeyboardEventDispatcher()
    private val keyboardPan: DirectionalPan<P> by lazy {
        DirectionalPan(wormhole = wormhole) {
            parentMonitor.repaint()
            repaint()
        }
    }
    private val mouse: TemporariesMouseEventDispatcher = CanvasBoundMouseEventDispatcher(input)
    private lateinit var mousePan: PanHelper
    private val highlighter = Canvas()
    private val selector = Canvas()
    private val selectionHelper: SelectionHelper<T, P> = SelectionHelper()
    private val selection: ObservableMap<Node<T>, P> = FXCollections.observableHashMap()
    private val selectionCandidates: ObservableMap<Node<T>, P> = FXCollections.observableHashMap()
    private val selectedElements: ImmutableMap<Node<T>, P>
        get() = ImmutableMap.copyOf(selection)
    private val selectionCandidatesMutex: Semaphore = Semaphore(1)
    private val zoomManager: ZoomManager by lazy {
        ExponentialZoomManager(this.wormhole.zoom, ExponentialZoomManager.DEF_BASE)
    }
    @Volatile private var feedback: Map<Interaction, List<() -> Unit>> = emptyMap()
    private val runMutex: Semaphore = Semaphore(1)

    /**
     * Invokes a given command on the simulation.
     */
    private val invokeOnSimulation: (Simulation<*, *>.() -> Unit) -> Unit
        get() = environment?.simulation?.let { { exec: Simulation<*, *>.() -> Unit -> it.schedule { exec.invoke(it) } } }
            ?: throw IllegalStateException("Uninitialized environment or simulation")

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
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPan += Direction2D.NORTH }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPan -= Direction2D.NORTH }
        }
        Keybinds[ActionFromKey.PAN_SOUTH].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPan += Direction2D.SOUTH }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPan -= Direction2D.SOUTH }
        }
        Keybinds[ActionFromKey.PAN_EAST].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPan += Direction2D.EAST }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPan -= Direction2D.EAST }
        }
        Keybinds[ActionFromKey.PAN_WEST].ifPresent {
            keyboard[ActionOnKey.PRESSED with it] = { keyboardPan += Direction2D.WEST }
            keyboard[ActionOnKey.RELEASED with it] = { keyboardPan -= Direction2D.WEST }
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
                        nodesToMove.values.maxWith(Comparator { a, b ->
                            (b - a).let { it.x + it.y }.roundToInt()
                        })?.let {
                            mousePosition - it
                        }?.let { offset ->
                            environment?.let { env ->
                                nodesToMove.forEach {
                                    env.moveNode(it.key, offset)
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
                    if (getStatus() == Status.PAUSED) {
                        goToStep(getStep() + 1)
                    }
                }
            }
        }

        // select
        mouse[MouseButtonTriggerAction(ActionOnMouse.PRESSED, MouseButton.SECONDARY)] = this::onSelectInitiated
        mouse[MouseButtonTriggerAction(ActionOnMouse.DRAGGED, MouseButton.SECONDARY)] = this::onSelecting
        mouse[MouseButtonTriggerAction(ActionOnMouse.RELEASED, MouseButton.SECONDARY)] = this::onSelected
        mouse[MouseButtonTriggerAction(ActionOnMouse.EXITED, MouseButton.SECONDARY)] = this::onSelectCanceled

        // primary mouse button
        mouse[MouseButtonTriggerAction(ActionOnMouse.PRESSED, MouseButton.PRIMARY)] = {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanInitiated(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectInitiated(it)
                else -> { }
            }
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.DRAGGED, MouseButton.PRIMARY)] = {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanning(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelecting(it)
                else -> { }
            }
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.RELEASED, MouseButton.PRIMARY)] = {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanned(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelected(it)
                else -> { }
            }
        }
        mouse[MouseButtonTriggerAction(ActionOnMouse.EXITED, MouseButton.PRIMARY)] = {
            when (parentMonitor.viewStatus) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanCanceled(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectCanceled(it)
                else -> { }
            }
        }

        // scroll
        input.setOnScroll {
            zoomManager.inc(it.deltaY / ZOOM_SCALE)
            wormhole.zoomOnPoint(it.unibo.alchemist.kotlin.makePoint(it.x, it.y), zoomManager.zoom)
            parentMonitor.repaint()
            it.consume()
        }

        selection.addListener(MapChangeListener {
            feedback += Interaction.HIGHLIGHTED to selection.map { paintHighlight(it.value, Colors.alreadySelected) }
            repaint()
        })
        selectionCandidates.addListener(MapChangeListener {
            feedback += Interaction.HIGHLIGHT_CANDIDATE to selectionCandidates.map { paintHighlight(it.value, Colors.selecting) }
            repaint()
        })
    }

    /**
     * Called when a pan gesture is initiated.
     * @param event the caller
     */
    private fun onPanInitiated(event: MouseEvent) {
        mousePan = PanHelper(makePoint(event.x, event.y))
    }

    /**
     * Called while a pan gesture is in progress and the view is moving.
     * @param event the caller
     */
    private fun onPanning(event: MouseEvent) {
        if (mousePan.valid) {
            wormhole.viewPosition = mousePan.update(makePoint(event.x, event.y), wormhole.viewPosition)
            parentMonitor.repaint()
        }
        event.consume()
    }

    /**
     * Called when a pan gesture finishes.
     */
    private fun onPanned(event: MouseEvent) {
        mousePan.close()
        event.consume()
    }

    /**
     * Called when a pan gesture is canceled.
     */
    private fun onPanCanceled(event: MouseEvent) {
        mousePan.close()
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
            it.update(it.unibo.alchemist.kotlin.makePoint(event.x, event.y))
            feedback += Interaction.SELECTION_BOX to listOf(selector.createDrawCommand(it.rectangle, Colors.selectionBox))
            addNodesToSelectionCandidates()
            repaint()
        }
        event.consume()
    }

    /**
     * Called when a select gesture finishes.
     */
    private fun onSelected(event: MouseEvent) {
        if (Keybinds[ActionFromKey.MODIFIER_CONTROL].filter { keyboard.isHeld(it).not() }.isPresent) {
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
        selectionCandidatesMutex.acquireUninterruptibly()
        selectionHelper.close()
        selectionCandidates.clear()
        selectionCandidatesMutex.release()
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
    private fun paintHighlight(position: P, paint: Paint): () -> Unit = {
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
            clearCanvas(selector)
            clearCanvas(highlighter)
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

    private class Colors {
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
}

/**
 * Cardinal and intercardinal directions.
 * @param x the X-value. Grows positively towards the "right".
 * @param y the Y-value. Grows positively upwards.
 */
enum class Direction2D(val x: Int, val y: Int) {
    NONE(0, 0),
    NORTH(0, 1),
    SOUTH(0, -1),
    EAST(1, 0),
    WEST(-1, 0),
    NORTHEAST(1, 1),
    SOUTHEAST(1, -1),
    SOUTHWEST(-1, -1),
    NORTHWEST(-1, 1);

    private fun flip(xFlip: Boolean, yFlip: Boolean): Direction2D =
        Direction2D.values().find {
            it.x == (if (xFlip) -x else x) && it.y == (if (yFlip) -y else y)
        } ?: Direction2D.NONE

    /**
     * Flips the direction horizontally and vertically
     */
    val flipped: Direction2D
        get() = flip(true, true)

    /**
     * Flips the direction's X-values
     */
    val flippedX: Direction2D
        get() = flip(true, false)

    /**
     * Flips the direction's Y-values
     */
    val flippedY: Direction2D
        get() = flip(false, true)

    private fun Int.limited(): Int = min(1, max(this, -1))

    /**
     * Sums with a direction.
     */
    operator fun plus(other: Direction2D): Direction2D =
        Direction2D.values().find {
            it.x == (x + other.x).limited() && it.y == (y + other.y).limited()
        } ?: Direction2D.NONE

    /**
     * Subtracts with a direction.
     */
    operator fun minus(other: Direction2D): Direction2D =
        Direction2D.values().find {
            it.x == (x - other.x).limited() && it.y == (y - other.y).limited()
        } ?: Direction2D.NONE

    /**
     * Multiplies by a scalar.
     */
    operator fun times(scalar: Int): Point = makePoint(x * scalar, y * scalar)

    /**
     * Returns whether this direction contains [other].
     * Specifically, a direction "D" contains another if D [Direction2D.plus] [other] equals D.
     * For example, [NORTHEAST] contains [NORTH] and [EAST], but not [WEST].
     * All directions contain [NONE]. [NONE] contains only [NONE].
     */
    operator fun contains(other: Direction2D): Boolean {
        return this + other == this
    }
}

/**
 * Manages panning towards a cardinal (N, S, E, W) or intercardinal (NE, NW, SE, SW) direction.
 * When a direction is added, panning towards it begins and doesn't stop until the given direction is removed.
 * @param speed The speed of each movement.
 * @param period Amount of time (milliseconds) between each movement.
 * @param wormhole The wormhole used to pan.
 * @param updates A runnable which will be called whenever a panning movement occurs.
 */
class DirectionalPan<P : Position2D<P>>(
    private val speed: Int = 5,
    private val period: Long = 15,
    private val wormhole: BidimensionalWormhole<P>,
    private val updates: () -> Unit
) {
    private var timer: Timer = Timer()
    private var currentDirection: Direction2D = Direction2D.NONE

    /**
     * Inputs a movement towards a certain direction.
     */
    operator fun plusAssign(direction: Direction2D) {
        if (direction !in currentDirection) {
            redirect(currentDirection + direction)
        }
    }

    /**
     * Stops moving towards a certain direction.
     */
    operator fun minusAssign(direction: Direction2D) {
        if (direction in currentDirection) {
            redirect(currentDirection - direction)
        }
    }

    /**
     * Redirects the movement towards a given direction,
     * potentially stopping the movement altogether if the given direction is [Direction2D.NONE].
     */
    private fun redirect(direction: Direction2D) {
        if (direction == Direction2D.NONE) {
            timer.cancel()
            currentDirection = Direction2D.NONE
        } else {
            if (direction != currentDirection) {
                timer.cancel()
                currentDirection = direction
                // the X-axis seems to be flipped... (grows positively towards the "left")
                // so we flip the X-values of the directions
                direction.flippedX.let {
                    timer = fixedRateTimer(period = period) {
                        wormhole.viewPosition += it * speed
                        updates()
                    }
                }
            }
        }
    }
}

/**
 * Manages panning.
 */
class PanHelper(private var current: Point) {
    /**
     * Returns whether this [PanHelper] is still valid.
     * Invalidation happens when [close] is called, for example when the mouse goes out of bounds.
     */
    var valid: Boolean = true
        private set

    /**
     * Updates the panning position and returns it.
     * @param next the destination point
     * @param view the position of the view
     */
    fun update(next: Point, view: Point): Point = if (valid) {
        (view + next - current).also { current = next }
    } else {
        throw IllegalStateException("Unable to pan after finalizing the PanHelper")
    }

    /**
     * Closes the helper. This invalidates the [PanHelper]
     */
    fun close() {
        valid = false
    }
}

/**
 * Manages multi-element selection and click-selection.
 */
class SelectionHelper<T, P : Position2D<P>> {

    /**
     * Allows basic multi-element box selections.
     * @param anchorPoint the starting and unchanging [Point] of the selection
     */
    class SelectionBox(val anchorPoint: Point, private val movingPoint: Point = anchorPoint) {
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
    fun begin(point: Point): SelectionHelper<T, P> = apply {
        isSelecting = true
        selectionPoint = point
        box = SelectionBox(point)
    }

    /**
     * Updates the selection with a new point.
     */
    fun update(point: Point): SelectionHelper<T, P> = apply {
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
    fun clickSelection(
        nodes: Map<Node<T>, P>,
        wormhole: BidimensionalWormhole<P>
    ): Pair<Node<T>, P>? =
        selectionPoint?.let { point ->
            nodes.minBy { (nodes[it.key]!!).getDistanceTo(wormhole.getEnvPoint(point)) }?.let {
                Pair(it.key, it.value)
            }
        }

    /**
     * Retrieves the elements selected by box selection, thus possibly empty
     */
    fun boxSelection(
        nodes: Map<Node<T>, P>,
        wormhole: BidimensionalWormhole<P>
    ): Map<Node<T>, P> =
        box?.let {
            rectangle.intersectingNodes(nodes, wormhole)
        } ?: emptyMap()
}

/**
 * Returns a command for drawing the given rectangle on the caller canvas.
 */
private fun Canvas.createDrawCommand(rectangle: Rectangle, colour: Paint): () -> Unit = {
    graphicsContext2D.let {
        it.fill = colour
        it.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
    } }

/**
 * Returns the nodes intersecting with the caller rectangle.
 */
private fun <T, P : Position2D<P>> Rectangle.intersectingNodes(
    nodes: Map<Node<T>, P>,
    wormhole: BidimensionalWormhole<P>
): Map<Node<T>, P> = let { area -> nodes.filterValues { wormhole.getViewPoint(it) in area } }

private operator fun Rectangle.contains(point: Point): Boolean =
    point.x.toDouble() in x..(x + width) &&
        point.y.toDouble() in y..(y + height)

private fun Point.makeRectangleWith(other: Point): Rectangle = Rectangle(
    min(this.x, other.x).toDouble(),
    min(this.y, other.y).toDouble(),
    abs(this.x - other.x).toDouble(),
    abs(this.y - other.y).toDouble())
